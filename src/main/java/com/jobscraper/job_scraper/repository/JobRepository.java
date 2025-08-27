package com.jobscraper.job_scraper.repository;

import com.jobscraper.job_scraper.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    boolean existsByJobUrl(String url);
    List<Job> findByAlertedFalse();

    @org.springframework.data.jpa.repository.Query("select j.id from Job j where j.alerted = false")
    List<Integer> findIdsByAlertedFalse();
}
