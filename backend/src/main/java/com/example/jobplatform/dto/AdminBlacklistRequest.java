package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminBlacklistRequest {

    @NotBlank(message = "拉黑原因不能为空")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
