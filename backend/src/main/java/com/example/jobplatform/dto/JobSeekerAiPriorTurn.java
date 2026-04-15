package com.example.jobplatform.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 多轮对话中的历史一轮：用户原话 + 上一轮模型返回的 JSON（字符串或对象均可，避免反序列化失败）。
 */
public class JobSeekerAiPriorTurn {

    @NotBlank(message = "历史用户说法不能为空")
    @Size(max = 2000)
    private String userText;

    /**
     * 上一轮助手输出的 JSON；可为字符串或 JSON 对象。
     */
    private JsonNode assistantJson;

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public JsonNode getAssistantJson() {
        return assistantJson;
    }

    public void setAssistantJson(JsonNode assistantJson) {
        this.assistantJson = assistantJson;
    }
}
