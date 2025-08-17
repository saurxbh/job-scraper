package com.jobscraper.job_scraper.service;

import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import com.microsoft.playwright.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobScraperService {

    @Autowired
    private CompanyRepository companyRepository;

//    @Autowired
//    private JobPostingRepository jobPostingRepository;

    public void scrapeAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        System.out.println(companies.size());

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
            String selector = company.getSelector();
            System.out.println("Scraping: " + company.getName() + " | " + storedUrl);

            page.navigate(storedUrl);
            page.waitForSelector(selector);

            // Scroll to bottom to ensure lazy-loaded jobs appear
            int prevHeight = 0;
            while (true) {
                int currHeight = (int) page.evaluate("() => document.body.scrollHeight");
                if (currHeight == prevHeight) break; // no more content
                prevHeight = currHeight;

                page.mouse().wheel(0, 2000);
                page.waitForTimeout(1000); // give time for new jobs to load
            }

            // Example selectors - replace with custom per-company later
            Locator jobs = page.locator(selector);
            int jobCount = jobs.count();
            System.out.println("Jobs found for " + company.getName() + ": " + jobCount);

            for (int i = 0; i < jobCount; i++) {
                String title = jobs.nth(i).innerText();
                if (title.matches("(?i).*\\b(Principal|Principle|Staff|Lead|VP|Manager|iOS|Kotlin|Android|Head|Network|Machine|ML|AI|Distinguished|Security)\\b.*")) continue;

                String url = jobs.nth(i).getAttribute("href");
                if (url.startsWith("/")) url = processUrl(url, storedUrl);
                System.out.println(title + " -> " + url);
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