package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JobSaveRequest {

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 100, message = "岗位名称长度不能超过100位")
    private String title;

    @Size(max = 100, message = "岗位分类长度不能超过100位")
    private String category;

    @NotBlank(message = "工作城市不能为空")
    @Size(max = 100, message = "工作城市长度不能超过100位")
    private String city;

    @NotNull(message = "最低薪资不能为空")
    private Integer salaryMin;

    @NotNull(message = "最高薪资不能为空")
    private Integer salaryMax;

    @Size(max = 50, message = "学历要求长度不能超过50位")
    private String educationRequirement;

    @Size(max = 50, message = "经验要求长度不能超过50位")
    private String experienceRequirement;

    @NotBlank(message = "工作方式不能为空")
    private String workMode;

    @Size(max = 2000, message = "技能要求长度不能超过2000位")
    private String skillRequirements;

    @Size(max = 255, message = "残疾适配类型长度不能超过255位")
    private String disabilitySupportType;

    @Size(max = 500, message = "福利长度不能超过500位")
    private String welfare;

    @Size(max = 4000, message = "岗位描述长度不能超过4000位")
    private String jobDescription;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Integer salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Integer getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Integer salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getEducationRequirement() {
        return educationRequirement;
    }

    public void setEducationRequirement(String educationRequirement) {
        this.educationRequirement = educationRequirement;
    }

    public String getExperienceRequirement() {
        return experienceRequirement;
    }

    public void setExperienceRequirement(String experienceRequirement) {
        this.experienceRequirement = experienceRequirement;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getSkillRequirements() {
        return skillRequirements;
    }

    public void setSkillRequirements(String skillRequirements) {
        this.skillRequirements = skillRequirements;
    }

    public String getDisabilitySupportType() {
        return disabilitySupportType;
    }

    public void setDisabilitySupportType(String disabilitySupportType) {
        this.disabilitySupportType = disabilitySupportType;
    }

    public String getWelfare() {
        return welfare;
    }

    public void setWelfare(String welfare) {
        this.welfare = welfare;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
