package com.reins.bookstore.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reins.bookstore.entity.UserAuth;
import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;
    private final UserAuthRepository users;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(JwtService jwtService, UserAuthRepository users, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.users = users;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return unauthorized(response);
        }
        Long userId;
        try {
            userId = jwtService.verifyAndGetUserId(authorization.substring(7).trim());
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(response);
        }
        // 查询持久化账号状态，让禁用和角色变更立即生效；没有保存登录状态。
        UserAuth user = users.findByUserId(userId);
        if (user == null || Boolean.FALSE.equals(user.getEnable()) || user.getIdentity() == null) {
            return unauthorized(response);
        }
        request.setAttribute("userId", userId);
        request.setAttribute("identity", user.getIdentity());
        return true;
    }

    private boolean unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("error", "登录凭证缺失、无效或已过期"));
        return false;
    }
}
