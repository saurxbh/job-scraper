package com.jobscraper.job_scraper.controller;

import com.jobscraper.job_scraper.service.JobScraperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

    @Autowired
    private JobScraperService scraperService;

    @GetMapping("/run")
    public String runScraper() {
        scraperService.scrapeAllCompanies();
        return "Scraper executed!";
    }
}