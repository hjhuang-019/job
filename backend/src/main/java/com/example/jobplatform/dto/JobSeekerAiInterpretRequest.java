package com.example.jobplatform.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class JobSeekerAiInterpretRequest {

    @NotBlank(message = "场景不能为空")
    @Size(max = 64)
    private String scene;

    @NotBlank(message = "用户说法不能为空")
    @Size(max = 2000)
    private String userText;

    /**
     * 当前页上下文（JSON 对象）；使用 JsonNode 避免前端偶发类型不符导致整包反序列化失败。
     */
    private JsonNode pageContext;

    /**
     * 多轮历史（按时间顺序）；每条含当时用户说法与助手返回的完整 JSON 文本。
     */
    @Size(max = 8, message = "对话历史最多保留8轮")
    @Valid
    private List<JobSeekerAiPriorTurn> priorTurns;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public JsonNode getPageContext() {
        return pageContext;
    }

    public void setPageContext(JsonNode pageContext) {
        this.pageContext = pageContext;
    }

    public List<JobSeekerAiPriorTurn> getPriorTurns() {
        return priorTurns;
    }

    public void setPriorTurns(List<JobSeekerAiPriorTurn> priorTurns) {
        this.priorTurns = priorTurns;
    }
}
