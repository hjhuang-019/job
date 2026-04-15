package com.example.jobplatform.vo;

import java.time.LocalDateTime;

public class MessageVO {

    private Long id;
    private String messageType;
    private String title;
    private String content;
    private String relatedBusinessType;
    private Long relatedBusinessId;
    /** 新简历投递（APPLY_RECEIVED）时，对应岗位 ID，便于前端跳转投递记录页 */
    private Long relatedJobId;
    private Boolean read;
    private LocalDateTime readTime;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

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

    public String getRelatedBusinessType() {
        return relatedBusinessType;
    }

    public void setRelatedBusinessType(String relatedBusinessType) {
        this.relatedBusinessType = relatedBusinessType;
    }

    public Long getRelatedBusinessId() {
        return relatedBusinessId;
    }

    public void setRelatedBusinessId(Long relatedBusinessId) {
        this.relatedBusinessId = relatedBusinessId;
    }

    public Long getRelatedJobId() {
        return relatedJobId;
    }

    public void setRelatedJobId(Long relatedJobId) {
        this.relatedJobId = relatedJobId;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
