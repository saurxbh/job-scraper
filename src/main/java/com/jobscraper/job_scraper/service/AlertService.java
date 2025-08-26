package com.jobscraper.job_scraper.service;

import com.jobscraper.job_scraper.entity.Job;
import com.jobscraper.job_scraper.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    @Autowired
    public AlertService(JobRepository jobRepository, NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    @Scheduled(fixedRate = 40 * 60 * 1000, initialDelay = 20 * 60 * 1000)
    public void sendAlertsForNewJobs() {
        // 1. Fetch jobs not yet alerted
        List<Job> newJobs = jobRepository.findByAlertedFalse();

        if (newJobs.isEmpty()) {
            System.out.println("No new jobs to alert.");
            return;
        }

        System.out.println("Sending alert for " + newJobs.size() + " new job(s)...");

        // 2. Build the alert message
        StringBuilder message = new StringBuilder("URGENT: These are the jobs posted in the last 30 minutes. Apply immediately!!!\n\n");
        for (Job job : newJobs) {
            message.append(job.getTitle())
                    .append(" :: ").append(job.getCompany())
                    .append(" :: ").append(job.getJobUrl())
                    .append("\n");
        }

        // 3. Send notification (for now, just console)
        notificationService.notify(message.toString());
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Alerting done at " + now.getHour() + ":" + now.getMinute());

        // 4. Mark jobs as alerted
        newJobs.forEach(job -> job.setAlerted(true));
        jobRepository.saveAll(newJobs);
        System.out.println("Jobs updated.");
    }
}
