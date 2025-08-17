package com.jobscraper.job_scraper.controller;

import com.jobscraper.job_scraper.entity.Job;
import com.jobscraper.job_scraper.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // Simple GET endpoint to trigger alerting manually
    @GetMapping("/notify")
    public ResponseEntity<String> notifyNewJobs() {
        alertService.sendAlertsForNewJobs();
        return ResponseEntity.ok("Alerting ran!");
    }
}