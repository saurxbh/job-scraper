package com.jobscraper.job_scraper.controller;

import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping
    public Company addCompany(@RequestBody Company company) {
        return companyRepository.save(company);
    }
}
