package com.example.jobplatform.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class JobSeekerProfileUpdateRequest {

    @Size(max = 50, message = "真实姓名长度不能超过50位")
    private String realName;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    @Size(max = 100, message = "残疾类型长度不能超过100位")
    private String disabilityType;

    @Size(max = 20, message = "残疾等级长度不能超过20位")
    private String disabilityLevel;

    @Size(max = 1000, message = "技能标签长度不能超过1000位")
    private String skills;

    @Size(max = 100, message = "期望城市长度不能超过100位")
    private String expectedCity;

    @Size(max = 50, message = "期望薪资长度不能超过50位")
    private String expectedSalary;

    @Size(max = 100, message = "求职意向长度不能超过100位")
    private String expectedJob;

    private Boolean acceptRemote;

    @Size(max = 500, message = "个人简介长度不能超过500位")
    private String introduction;

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
}
