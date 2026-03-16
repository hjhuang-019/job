package com.example.jobplatform.security;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtils jwtUtils, SysUserMapper sysUserMapper, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.sysUserMapper = sysUserMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(TOKEN_PREFIX)) {
            writeUnauthorized(response, "未登录或Token缺失");
            return false;
        }

        String token = authorization.substring(TOKEN_PREFIX.length()).trim();
        if (!jwtUtils.validateToken(token)) {
            writeUnauthorized(response, "Token无效或已过期");
            return false;
        }

        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            writeUnauthorized(response, "Token用户信息无效");
            return false;
        }

        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            writeUnauthorized(response, "用户不存在");
            return false;
        }
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            writeUnauthorized(response, "当前账号已被禁用");
            return false;
        }

        UserContext.set(new LoginUser(sysUser.getId(), sysUser.getUsername(), sysUser.getRole()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, message)));
    }
}
