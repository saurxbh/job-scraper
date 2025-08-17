package com.jobscraper.job_scraper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void notify(String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("saurabh@techsmail.com");
            message.setSubject("URGENT: RECENT JOBS, APPLY IMMEDIATELY!");
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Failed to send email: " + e.getMessage());
            // Optionally, rethrow as a custom exception if you want upstream handling
        }
    }
}


