package com.jobscraper.job_scraper.event;

import java.time.LocalDateTime;
import java.util.List;

public class ScrapeCompletedEvent {
    private String runId;
    private LocalDateTime completedAt;
    private int newJobsCount;
    private List<Integer> newJobIds;

    public ScrapeCompletedEvent() {}

    public ScrapeCompletedEvent(String runId, LocalDateTime completedAt, int newJobsCount,
                                List<Integer> newJobIds) {
        this.runId = runId;
        this.completedAt = completedAt;
        this.newJobsCount = newJobsCount;
        this.newJobIds = newJobIds;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getNewJobsCount() {
        return newJobsCount;
    }

    public void setNewJobsCount(int newJobsCount) {
        this.newJobsCount = newJobsCount;
    }

    public List<Integer> getNewJobIds() {
        return newJobIds;
    }

    public void setNewJobIds(List<Integer> newJobIds) {
        this.newJobIds = newJobIds;
    }
}
