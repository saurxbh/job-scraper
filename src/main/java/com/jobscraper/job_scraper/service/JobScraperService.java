package com.jobscraper.job_scraper.service;

import com.jobscraper.job_scraper.config.KafkaConfig;
import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.entity.FilterSpec;
import com.jobscraper.job_scraper.entity.FilterType;
import com.jobscraper.job_scraper.entity.Job;
import com.jobscraper.job_scraper.event.ScrapeCompletedEvent;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import com.jobscraper.job_scraper.repository.JobRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


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
        //if (company.getName().equalsIgnoreCase("verizon")) return;

        Response response = page.navigate(storedUrl);
        if (response.status() >= 400) {
            System.out.println("Something went wrong. Response Status: " + response.status());
            return;
        }
        handleCookieBanner(page);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                page.waitForSelector(textSelector, new Page.WaitForSelectorOptions().setTimeout(60000).setState(WaitForSelectorState.VISIBLE));
                if (company.isDynamic()) applyFilters(page, company);
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

    private String processUrl(String url, String storedUrl) {
        int idx = storedUrl.indexOf("/", storedUrl.indexOf("//") + 2);
        String baseUrl = idx != -1 ? storedUrl.substring(0, idx) : storedUrl;

        url = url.substring(1);

        return baseUrl + "/" + url;
    }

    private void handleCookieBanner(Page page) {
        try {
            Locator accept = page.locator("text=Accept"); // or a CSS selector for the button
            if (accept.isVisible()) {
                accept.click();
                System.out.println("Cookie banner accepted.");
            }
        } catch (Exception e) {
            // ignore if not present
            System.out.println("No cookie banner.");
        }
    }

    private void applyFilters(Page page, Company company) {
        List<FilterSpec> cfg = company.getFilterConfig();
        if (cfg == null || cfg.isEmpty()) return;

        for (FilterSpec e : cfg) {
            String label = e.label();
            FilterType type = e.type();
            String selector = e.selector();
            List<String> permissibleValues = e.permissibleValues();

            switch (type) {
                case BUTTON -> {
                    Locator button = page.locator(selector).first();
                    button.click();
                }
                case SELECT -> {
                    Locator dropdown = page.locator(selector).first();
                    String tag = dropdown.evaluate("el => el.tagName").toString();

                    for (String v : permissibleValues) {
                        if ("select".equalsIgnoreCase(tag)) {
                            try {
                                dropdown.selectOption(new SelectOption().setLabel(v));
                            } catch (Exception ignore) {
                                dropdown.selectOption(v);
                            }
                        } else {
                            // for else, not taking comma separated values as is as is the case in the if block, passing selectors here.
//                            dropdown.click();
//                            Locator option = page.locator(v).first();
//                            option.click();
                        }
                    }
                }
                default -> {}
            }
        }
    }

    private void scrollToBottom(Page page) {
        int prevHeight = -1;
        int sameHeightCount = 0;

        while (true) {
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            page.waitForTimeout(1000);

            int currHeight = (int) page.evaluate("() => document.body.scrollHeight");
            if (currHeight == prevHeight) {
                sameHeightCount++;
            } else {
                sameHeightCount = 0; // reset if new content was added
            }
            prevHeight = currHeight;

            if (sameHeightCount >= 2) {
                break;
            }
        }
        System.out.println("Reached bottom of the page.");
    }
}