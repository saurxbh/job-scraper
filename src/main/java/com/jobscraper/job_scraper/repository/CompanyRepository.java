package com.jobscraper.job_scraper.repository;

import com.jobscraper.job_scraper.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}