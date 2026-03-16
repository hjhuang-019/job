package com.example.jobplatform.vo;

public class LoginResponseVO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private CurrentUserVO userInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public CurrentUserVO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(CurrentUserVO userInfo) {
        this.userInfo = userInfo;
    }
}
