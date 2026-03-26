package com.example.jobplatform.vo;

import java.time.LocalDateTime;

public class ApplicationVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String enterpriseName;
    private Long jobSeekerUserId;
    private String jobSeekerName;
    private Long resumeId;
    private String resumeName;
    private String status;
    private String remark;
    private LocalDateTime applyTime;
    private LocalDateTime handledTime;

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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public Long getJobSeekerUserId() {
        return jobSeekerUserId;
    }

    public void setJobSeekerUserId(Long jobSeekerUserId) {
        this.jobSeekerUserId = jobSeekerUserId;
    }

    public String getJobSeekerName() {
        return jobSeekerName;
    }

    public void setJobSeekerName(String jobSeekerName) {
        this.jobSeekerName = jobSeekerName;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeName() {
        return resumeName;
    }

    public void setResumeName(String resumeName) {
        this.resumeName = resumeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getHandledTime() {
        return handledTime;
    }

    public void setHandledTime(LocalDateTime handledTime) {
        this.handledTime = handledTime;
    }
}
