package com.jobscraper.job_scraper.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_company_title", columnList = "company, title"),
        @Index(name = "idx_scraped_at", columnList = "scrapedAt")
})
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 1000)
    private String jobUrl;

    private String location;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;

    @Column(name = "job_hash", unique = true)
    private String jobHash; // For deduplication

    // Constructors
    public Job() {}

    public Job(String company, String title, String jobUrl, String location) {
        this.company = company;
        this.title = title;
        this.jobUrl = jobUrl;
        this.location = location;
        this.scrapedAt = LocalDateTime.now();
        this.jobHash = generateJobHash(company, title, location);
    }

    // Generate unique hash for deduplication
    private String generateJobHash(String company, String title, String location) {
        String combined = (company + title + location).toLowerCase().replaceAll("\\s+", "");
        return String.valueOf(combined.hashCode());
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getScrapedAt() { return scrapedAt; }
    public void setScrapedAt(LocalDateTime scrapedAt) { this.scrapedAt = scrapedAt; }

    public String getJobHash() { return jobHash; }
    public void setJobHash(String jobHash) { this.jobHash = jobHash; }

    @Override
    public String toString() {
        return "Job{" +
                "company='" + company + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", scrapedAt=" + scrapedAt +
                '}';
    }
}