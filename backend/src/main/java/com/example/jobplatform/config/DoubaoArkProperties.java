package com.example.jobplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "doubao.ark")
public class DoubaoArkProperties {

    /**
     * 火山方舟 OpenAPI 根路径，默认北京区 chat/completions。
     */
    private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";

    /**
     * API Key，建议使用环境变量 DOUBAO_ARK_API_KEY 注入，勿提交到仓库。
     */
    private String apiKey = "";

    /**
     * 推理接入点 ID，例如 ep-xxxx。
     */
    private String model = "";

    private int maxUserChars = 2000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxUserChars() {
        return maxUserChars;
    }

    public void setMaxUserChars(int maxUserChars) {
        this.maxUserChars = maxUserChars;
    }
}
