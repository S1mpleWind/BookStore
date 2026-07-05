package com.reins.bookstore.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Session 拦截器
 *
 * 作用：拦截所有 /api/v1/** 的 HTTP 请求，检查用户是否已登录。
 *
 * 工作原理：
 * 1. 放行 OPTIONS 请求（浏览器 CORS 预检请求）
 * 2. 从 request 中获取 Session（不创建新的）
 * 3. 如果 Session 存在且包含 userId 属性 → 已登录，放行
 * 4. 否则 → 返回 HTTP 401（未授权）
 *
 * 注意：登录和注册接口在 SessionConfig 中被排除，不经过此拦截器。
 *
 * 相比 JWT 的方案，Session 方式的优势是：
 * - 服务端可以主动使 Session 失效（踢人下线）
 * - 敏感数据存在服务端，客户端只存一个 Session ID
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 请求——浏览器发送跨域请求前会先发一个 OPTIONS 预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 检查 Session：getSession(false) 不创建新 Session
        // 如果用户没登录过或 Session 已过期，返回 null
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return true;  // 已登录，放行
        }

        // 未登录或 session 过期 → 返回 401
        response.setStatus(401);
        return false;
    }
}
