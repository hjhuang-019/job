package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminFeedbackResolveRequest {

    @NotBlank(message = "处理备注不能为空")
    @Size(max = 500, message = "处理备注不能超过500字")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
