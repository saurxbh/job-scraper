package com.jobscraper.job_scraper.repository;

import com.jobscraper.job_scraper.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

}
