package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminVerifyRequest {

    @NotBlank(message = "审核结果不能为空")
    private String status;

    @Size(max = 500, message = "审核备注长度不能超过500位")
    private String comment;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
