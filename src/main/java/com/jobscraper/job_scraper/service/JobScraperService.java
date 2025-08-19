package com.jobscraper.job_scraper.service;

import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.entity.Job;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import com.jobscraper.job_scraper.repository.JobRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobScraperService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Scheduled(fixedRate = 40 * 60 * 1000)
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scrapeCompany(Company company, Page page) {
        try {
            String storedUrl = company.getCareerPageUrl();
            String textSelector = company.getTextSelector();
            String linkSelector = company.getLinkSelector();
            System.out.println("Scraping: " + company.getName());

            page.navigate(storedUrl);

            // Scroll to bottom to ensure lazy-loaded jobs appear
            int prevHeight = 0;
            while (true) {
                int currHeight = (int) page.evaluate("() => document.body.scrollHeight");
                if (currHeight == prevHeight) break; // no more content
                prevHeight = currHeight;

                page.mouse().wheel(0, 2000);
                page.waitForTimeout(1000); // give time for new jobs to load
            }

            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForSelector("a.stretched-link", new Page.WaitForSelectorOptions().setTimeout(60000));

            // Example selectors - replace with custom per-company later
            Locator jobTitles = page.locator(textSelector);
            Locator jobLinks = page.locator(linkSelector);
            int jobCount = jobTitles.count();
            int linksCount = jobLinks.count();
            System.out.println("Jobs found for " + company.getName() + ": " + jobCount + ", " + linksCount + " links.");

            for (int i = 0; i < jobCount; i++) {
                String title = jobTitles.nth(i).innerText();
                if (title.matches("(?i).*(Mobile|Architect|SRE|PhD|Research|Director|Principal|Principle|Staff|Lead|VP|Manager|iOS|Kotlin|Android|Head|Network|Machine|ML|AI|Distinguished|Security|Strategist).*")) continue;

                String url = jobLinks.nth(i).getAttribute("href");
                if (url.startsWith("/")) url = processUrl(url, storedUrl);

                if (!jobRepository.existsByJobUrl(url)) {
                    Job job = new Job();
                    job.setCompany(company);
                    job.setTitle(title);
                    job.setJobUrl(url);
                    //job.setScrapedAt(LocalDateTime.now());

                    jobRepository.save(job);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to scrape " + company.getCareerPageUrl());
            e.printStackTrace();
        }
    }

    private String processUrl(String url, String storedUrl) {
        int idx = storedUrl.indexOf("/", storedUrl.indexOf("//") + 2);
        String baseUrl = idx != -1 ? storedUrl.substring(0, idx) : storedUrl;

        url = url.substring(1);

        return baseUrl + "/" + url;
    }
}