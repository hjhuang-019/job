package com.example.jobplatform.entity;

import java.time.LocalDateTime;

public class FavoriteJob {

    private Long id;
    private Long jobId;
    private Long jobSeekerUserId;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobSeekerUserId() {
        return jobSeekerUserId;
    }

    public void setJobSeekerUserId(Long jobSeekerUserId) {
        this.jobSeekerUserId = jobSeekerUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
