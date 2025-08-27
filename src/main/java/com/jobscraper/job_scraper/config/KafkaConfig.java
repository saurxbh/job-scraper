package com.jobscraper.job_scraper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaConfig {
    public static final String TOPIC = "jobs.scrape.completed";

    @Bean
    public NewTopic scrapeCompletedTopic() {
        return new NewTopic(TOPIC, 1, (short)1);
    }
}