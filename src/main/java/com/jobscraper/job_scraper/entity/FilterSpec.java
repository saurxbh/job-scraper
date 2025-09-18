package com.jobscraper.job_scraper.entity;

import java.util.List;

public record FilterSpec(
        String label,
        FilterType type,
        String selector,
        List<String> permissibleValues
) {
}
