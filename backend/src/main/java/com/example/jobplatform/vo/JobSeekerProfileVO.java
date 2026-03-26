package com.example.jobplatform.vo;

public class JobSeekerProfileVO {

    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String disabilityType;
    private String disabilityLevel;
    private String skills;
    private String expectedCity;
    private String expectedSalary;
    private String expectedJob;
    private Boolean acceptRemote;
    private String introduction;
    private String certificatePath;
    private String verifyStatus;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisabilityType() {
        return disabilityType;
    }

    public void setDisabilityType(String disabilityType) {
        this.disabilityType = disabilityType;
    }

    public String getDisabilityLevel() {
        return disabilityLevel;
    }

    public void setDisabilityLevel(String disabilityLevel) {
        this.disabilityLevel = disabilityLevel;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExpectedCity() {
        return expectedCity;
    }

    public void setExpectedCity(String expectedCity) {
        this.expectedCity = expectedCity;
    }

    public String getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(String expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getExpectedJob() {
        return expectedJob;
    }

    public void setExpectedJob(String expectedJob) {
        this.expectedJob = expectedJob;
    }

    public Boolean getAcceptRemote() {
        return acceptRemote;
    }

    public void setAcceptRemote(Boolean acceptRemote) {
        this.acceptRemote = acceptRemote;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus;
    }
}
