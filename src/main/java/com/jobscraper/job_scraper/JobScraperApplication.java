package com.jobscraper.job_scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobScraperApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobScraperApplication.class, args);
	}

}
