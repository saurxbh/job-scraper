package com.jobscraper.job_scraper.entity;

import jakarta.persistence.*;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // relation instead of string

    private String title;

    private String jobUrl;

    // Constructors
    public Job() {}

    public Job(Company company, String title, String jobUrl) {
        this.company = company;
        this.title = title;
        this.jobUrl = jobUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Company getCompany() { return company; }

    public void setCompany(Company company) { this.company = company; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getJobUrl() { return jobUrl; }

    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
}