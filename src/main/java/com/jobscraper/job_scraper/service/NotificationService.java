package com.jobscraper.job_scraper.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void notify(String message) {
        // For now, just print — later can hook in email/Slack/etc.
        System.out.println("=== ALERT ===\n" + message);
    }
}


