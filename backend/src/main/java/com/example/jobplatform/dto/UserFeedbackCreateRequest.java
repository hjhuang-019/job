package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserFeedbackCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 150, message = "标题不能超过150字")
    private String title;

    @NotBlank(message = "正文不能为空")
    @Size(max = 2000, message = "正文不能超过2000字")
    private String content;

    /**
     * 可选：GENERAL（一般咨询）、BLACKLIST_APPEAL（黑名单申诉，仅黑名单用户可选）。
     * 缺省按 GENERAL 处理。
     */
    private String category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
