package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResumeSaveRequest {

    @NotBlank(message = "简历标题不能为空")
    @Size(max = 100, message = "简历标题长度不能超过100位")
    private String title;

    @Size(max = 2000, message = "教育经历长度不能超过2000位")
    private String education;

    @Size(max = 2000, message = "工作经历长度不能超过2000位")
    private String experience;

    @Size(max = 2000, message = "项目经历长度不能超过2000位")
    private String projectExperience;

    @Size(max = 2000, message = "技能长度不能超过2000位")
    private String skills;

    @Size(max = 4000, message = "自我评价长度不能超过4000位")
    private String selfEvaluation;

    private Boolean isDefault;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getProjectExperience() {
        return projectExperience;
    }

    public void setProjectExperience(String projectExperience) {
        this.projectExperience = projectExperience;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getSelfEvaluation() {
        return selfEvaluation;
    }

    public void setSelfEvaluation(String selfEvaluation) {
        this.selfEvaluation = selfEvaluation;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
