package com.example.jobplatform.vo;

public class AdminStatisticsVO {

    private Long userCount;
    private Long enterpriseCount;
    private Long jobCount;
    private Long applicationCount;

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getEnterpriseCount() {
        return enterpriseCount;
    }

    public void setEnterpriseCount(Long enterpriseCount) {
        this.enterpriseCount = enterpriseCount;
    }

    public Long getJobCount() {
        return jobCount;
    }

    public void setJobCount(Long jobCount) {
        this.jobCount = jobCount;
    }

    public Long getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(Long applicationCount) {
        this.applicationCount = applicationCount;
    }
}
