package com.example.jobplatform.vo;

import java.util.Collections;
import java.util.Map;

public class JobSeekerAiInterpretVO {

    private String intent = "GENERAL";
    private String spokenReply = "";
    private Map<String, Object> profile = Collections.emptyMap();
    private Map<String, Object> resume = Collections.emptyMap();
    private Map<String, Object> jobFilters = Collections.emptyMap();
    private Map<String, Object> navigation = Collections.emptyMap();
    private Map<String, Object> apply = Collections.emptyMap();

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getSpokenReply() {
        return spokenReply;
    }

    public void setSpokenReply(String spokenReply) {
        this.spokenReply = spokenReply;
    }

    public Map<String, Object> getProfile() {
        return profile;
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile;
    }

    public Map<String, Object> getResume() {
        return resume;
    }

    public void setResume(Map<String, Object> resume) {
        this.resume = resume;
    }

    public Map<String, Object> getJobFilters() {
        return jobFilters;
    }

    public void setJobFilters(Map<String, Object> jobFilters) {
        this.jobFilters = jobFilters;
    }

    public Map<String, Object> getNavigation() {
        return navigation;
    }

    public void setNavigation(Map<String, Object> navigation) {
        this.navigation = navigation;
    }

    public Map<String, Object> getApply() {
        return apply;
    }

    public void setApply(Map<String, Object> apply) {
        this.apply = apply;
    }
}
