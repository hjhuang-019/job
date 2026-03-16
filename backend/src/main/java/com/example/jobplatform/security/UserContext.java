package com.example.jobplatform.security;

public final class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        USER_HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static String getUsername() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUsername();
    }

    public static String getRole() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getRole();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
