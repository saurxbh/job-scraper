package com.jobscraper.job_scraper.controller;

import com.jobscraper.job_scraper.entity.Company;
import com.jobscraper.job_scraper.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping
    public ResponseEntity<Company> addCompany(@RequestBody Company company) {
        Company savedCompany = companyRepository.save(company);
        URI location = URI.create("/api/companies/" + savedCompany.getId());
        return ResponseEntity.created(location).body(savedCompany);
    }

    @GetMapping("/{id}")
    public Company getCompany(@PathVariable Long id) {
        return companyRepository.findById(id).orElseThrow();
    }

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }
}