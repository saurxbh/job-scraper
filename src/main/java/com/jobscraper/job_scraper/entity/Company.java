package com.jobscraper.job_scraper.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // consistency with Job
    private Long id;

    private String name;

    @Column(length = 1000)
    private String careerPageUrl;

    private String textSelector; // CSS selector for the link text
    private String linkSelector; // selector for the actual link

    @JsonProperty("isDynamic")
    @Column(name = "is_dynamic", nullable = false)
    private boolean dynamic = false;

    @JsonProperty("filters")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter_config", columnDefinition = "jsonb", nullable = false)
    private List<FilterSpec> filterConfig = new ArrayList<>();

    // Constructors
    public Company() {}

    public Company(String name, String careerPageUrl, String textSelector, String linkSelector, boolean dynamic, List<FilterSpec> filterConfig) {
        this.name = name;
        this.careerPageUrl = careerPageUrl;
        this.textSelector = textSelector;
        this.linkSelector = linkSelector;
        this.dynamic = dynamic;
        this.filterConfig = filterConfig;
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

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public List<FilterSpec> getFilterConfig() {
        return filterConfig;
    }

    public void setFilterConfig(List<FilterSpec> filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public String toString() {
        return name;
    }
}