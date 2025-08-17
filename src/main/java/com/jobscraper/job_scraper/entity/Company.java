package com.jobscraper.job_scraper.entity;

import jakarta.persistence.*;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // consistency with Job
    private Long id;

    private String name;

    private String careerPageUrl;

    private String selector; // CSS selector for job links

    // Constructors
    public Company() {}

    public Company(String name, String careerPageUrl, String selector) {
        this.name = name;
        this.careerPageUrl = careerPageUrl;
        this.selector = selector;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getCareerPageUrl() { return careerPageUrl; }

    public void setCareerPageUrl(String careerPageUrl) { this.careerPageUrl = careerPageUrl; }

    public String getSelector() { return selector; }

    public void setSelector(String selector) { this.selector = selector; }
}