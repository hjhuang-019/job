package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApplicationStatusUpdateRequest {

    @NotBlank(message = "投递状态不能为空")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500位")
    private String remark;

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
}
