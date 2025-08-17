package com.jobscraper.job_scraper.entity;

import jakarta.persistence.*;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // consistency with Job
    private Long id;

    private String name;

    @Column(length = 1000)
    private String careerPageUrl;

    private String textSelector; // CSS selector for job links
    private String linkSelector; // Could be different, same for most cases

    // Constructors
    public Company() {}

    public Company(String name, String careerPageUrl, String textSelector, String linkSelector) {
        this.name = name;
        this.careerPageUrl = careerPageUrl;
        this.textSelector = textSelector;
        this.linkSelector = linkSelector;
    }


    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getCareerPageUrl() { return careerPageUrl; }

    public void setCareerPageUrl(String careerPageUrl) { this.careerPageUrl = careerPageUrl; }

    public String getTextSelector() {
        return textSelector;
    }

    public void setTextSelector(String textSelector) {
        this.textSelector = textSelector;
    }

    public String getLinkSelector() {
        return linkSelector;
    }

    public void setLinkSelector(String linkSelector) {
        this.linkSelector = linkSelector;
    }
}