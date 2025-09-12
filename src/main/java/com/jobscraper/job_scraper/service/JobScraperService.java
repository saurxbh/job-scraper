package com.jobscraper.job_scraper.service;

import com.jobscraper.job_scraper.config.KafkaConfig;
import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.entity.FilterSpec;
import com.jobscraper.job_scraper.entity.Job;
import com.jobscraper.job_scraper.event.ScrapeCompletedEvent;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import com.jobscraper.job_scraper.repository.JobRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class JobScraperService {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private KafkaTemplate<String, ScrapeCompletedEvent> kafkaTemplate;

    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void scrapeAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        System.out.println("Scraping for " + companies.size() + " companies.");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            for (Company company : companies) {
                scrapeCompany(company, page);
            }

            browser.close();
            List<Job> newJobs = jobRepository.findByAlertedFalse();
            List<Integer> newJobIds = jobRepository.findIdsByAlertedFalse();
            LocalDateTime time = LocalDateTime.now();
            String minutes = Integer.toString(time.getMinute());
            minutes = minutes.length() == 1 ? '0' + minutes : minutes;
            System.out.println("Scraping done at " + time.getHour() + ":" + minutes + ". New jobs found: " + newJobs.size());

            if (!newJobs.isEmpty()) {
                ScrapeCompletedEvent event = new ScrapeCompletedEvent(
                        time.toString(),
                        LocalDateTime.now(),
                        newJobs.size(),
                        newJobIds
                );
                kafkaTemplate.send(KafkaConfig.TOPIC, event);
            }

        } catch (Exception e) {
            System.out.println(RED + "Error while scraping. " + RESET);
        }
    }

    private void scrapeCompany(Company company, Page page) {
        String storedUrl = company.getCareerPageUrl();
        String textSelector = company.getTextSelector();
        String linkSelector = company.getLinkSelector();
        System.out.println("Scraping: " + company.getName());
        if (company.isDynamic()) return;

        Response response = page.navigate(storedUrl);
        if (response.status() >= 400) {
            System.out.println("Something went wrong. Response Status: " + response.status());
            return;
        }
        handleCookieBanner(page);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                if (company.isDynamic()) applyFilters(company, page);
                page.waitForSelector(textSelector, new Page.WaitForSelectorOptions().setTimeout(60000).setState(WaitForSelectorState.VISIBLE));
                scrollToBottom(page);

                Locator jobTitles = page.locator(textSelector);
                Locator jobLinks = page.locator(linkSelector);
                int jobCount = jobTitles.count();
                int linksCount = jobLinks.count();
                System.out.println("Jobs found for " + company.getName() + ": " + jobCount + ", " + linksCount + " link(s).");

                for (int i = 0; i < jobCount; i++) {
                    String title = jobTitles.nth(i).innerText();
                    if (title.matches("(?i).*(Infrastructure|Mobile|Architect|SRE|PhD|Research|Director|President|Principal|Principle|Staff|Lead|VP|Manager|iOS|Kotlin|Android|Head|Network|Machine|ML|AI|Distinguished|Security|Strategist|Support|Spark|SAP|Appian|ODM|Reliability|Hardware|Firmware).*"))
                        continue;

                    String url = jobLinks.nth(i).getAttribute("href");
                    if (url.startsWith("/")) url = processUrl(url, storedUrl);

                    if (!jobRepository.existsByJobUrl(url)) {
                        Job job = new Job();
                        job.setCompany(company);
                        job.setTitle(title);
                        job.setJobUrl(url);
                        job.setCreatedAt(LocalDateTime.now());

                        jobRepository.save(job);
                    }
                }
                return;

            } catch (TimeoutError t) {
                if (!page.content().contains(textSelector)) {
                    System.out.println(RED + "No matching jobs found." + RESET);
                    return;
                }
                System.out.println("Attempt " + attempt + " failed for " + company);
                if (attempt < 3) {
                    System.out.println("Retrying...");
                } else {
                    System.out.println("Max retries reached for " + company + ". Skipping.");
                }
            } catch (Exception e) {
                System.out.println("Attempt " + attempt + " failed for " + company);
                if (attempt < 3) {
                    System.out.println("Retrying...");
                } else {
                    System.out.println("Max retries reached for " + company + ". Skipping.");
                }
            }
        }
    }

    private void applyFilters(Company company, Page page) {
        Map<String, FilterSpec> cfg = company.getFilterConfig();
        if (cfg == null || cfg.isEmpty()) return;
        boolean applyButtonPresent = false;
        String applyButtonSelector = null;

        for (Map.Entry<String, FilterSpec> e : cfg.entrySet()) {
            String label = e.getKey();
            FilterSpec spec = e.getValue();

            if ("Apply Filters".equalsIgnoreCase(label)) {
                applyButtonPresent = true;
                applyButtonSelector = spec.selector();
                continue;
            }

            String selector = spec.selector();
            Locator control = page.locator(selector);

            switch (spec.type()) {
                case SELECT -> {
                    String tagName = control.evaluate("el => el.tagName").toString();
                    for (String v : spec.permissibleValues()) {
                        if ("SELECT".equalsIgnoreCase(tagName)) {
                            // Real <select> element
                            try {
                                control.selectOption(new SelectOption().setLabel(v));
                            } catch (Exception ignore) {
                                control.selectOption(new SelectOption().setValue(v));
                            }
                        } else {
                            System.out.println(v);
                            control.click();
                            Locator option = page.locator(".option",
                                    new Page.LocatorOptions().setHasText(v));
                            option.click();
                        }
                    }
                }
                case INPUT -> {
                    String v = spec.permissibleValues().isEmpty() ? "" : spec.permissibleValues().get(0);
                    System.out.println(v);
                    control.fill("");
                    control.type(v);
                    control.press("Enter");
                }
                case CHECKBOX -> {
                    boolean shouldCheck = spec.permissibleValues().stream().findFirst()
                            .map(s -> s.equalsIgnoreCase("true")).orElse(true);
                    boolean isChecked = Boolean.TRUE.equals(control.isChecked());
                    if (shouldCheck && !isChecked) control.check();
                    if (!shouldCheck && isChecked)  control.uncheck();
                }
                case BUTTON -> {
                    control.click();
                }
                default -> System.out.println("Unknown filter type.");
            }
        }
        if (applyButtonPresent && applyButtonSelector != null) {
            page.locator(applyButtonSelector).click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("Filters applied");
        }

    }

    private String processUrl(String url, String storedUrl) {
        int idx = storedUrl.indexOf("/", storedUrl.indexOf("//") + 2);
        String baseUrl = idx != -1 ? storedUrl.substring(0, idx) : storedUrl;

        url = url.substring(1);

        return baseUrl + "/" + url;
    }

    private void handleCookieBanner(Page page) {
        try {
            Locator accept = page.locator("text=Accept Cookies"); // or a CSS selector for the button
            if (accept.isVisible()) {
                accept.click();
                System.out.println("Cookie banner accepted.");
            }
        } catch (Exception e) {
            // ignore if not present
            System.out.println("No cookie banner.");
        }
    }

    private void scrollToBottom(Page page) {
        int prevHeight = -1;
        int sameHeightCount = 0;

        while (true) {
            // Scroll down
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");

            // Wait a little to let new content load (important if jobs are lazy-loaded)
            page.waitForTimeout(1000);

            // Check current page height
            int currHeight = (int) page.evaluate("() => document.body.scrollHeight");

            if (currHeight == prevHeight) {
                sameHeightCount++;
            } else {
                sameHeightCount = 0; // reset if new content was added
            }

            prevHeight = currHeight;

            // Break after seeing no growth for 2 consecutive checks
            if (sameHeightCount >= 2) {
                break;
            }
        }
        System.out.println("Reached bottom of the page.");
    }
}