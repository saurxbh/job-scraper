package com.jobscraper.job_scraper.service;

import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
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

            System.out.println("Scraping: " + company.getName() + " | " + storedUrl);

            page.navigate(storedUrl);
            page.waitForSelector("a.vacancies-list-item__link");

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
            List<String> jobTitles = page.locator("a.vacancies-list-item__link").allInnerTexts();
            //List<String> jobUrls = page.locator("a.vacancies-list-item__link");

            System.out.println("Jobs found for " + company.getName() + ": " + jobTitles.size());
            for (int i = 0; i < jobTitles.size(); i++) {
                System.out.println(jobTitles.get(i));
            }

        } catch (Exception e) {
            System.err.println("Failed to scrape " + company.getCareerPageUrl());
            e.printStackTrace();
        }
    }
}