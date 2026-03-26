package com.example.jobplatform.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EnterpriseProfileUpdateRequest {

    @Size(max = 200, message = "企业名称长度不能超过200位")
    private String enterpriseName;

    @Size(max = 100, message = "所属行业长度不能超过100位")
    private String industry;

    @Size(max = 50, message = "企业规模长度不能超过50位")
    private String scaleType;

    @Size(max = 50, message = "联系人长度不能超过50位")
    private String contactPerson;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "联系电话格式不正确")
    private String contactPhone;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    @Size(max = 255, message = "地址长度不能超过255位")
    private String address;

    @Size(max = 1000, message = "企业简介长度不能超过1000位")
    private String description;

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getScaleType() {
        return scaleType;
    }

    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
