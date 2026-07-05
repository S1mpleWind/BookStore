package com.reins.bookstore.config;

import com.reins.bookstore.interceptor.SessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring 配置类
 *
 * 负责两件事：
 * 1. 注册 SessionInterceptor，拦截 API 请求做登录校验
 * 2. 配置 CORS，允许前端（localhost:5173）跨域访问后端
 *
 * 关于拦截器配置：
 * - 拦截所有 /api/v1/** 的请求——所有 API 都需要登录
 * - 排除 /login 和 /register——这两个接口不需要登录
 *
 * 关于 CORS 配置：
 * - allowedOrigins：只允许前端开发服务器的地址（安全限制）
 * - allowCredentials：允许携带 Cookie（Session 依赖 Cookie）
 *   如果不设置此项，前端 credentials: 'include' 会失效
 */
@Configuration
public class SessionConfig implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    /**
     * 注册拦截器，设定拦截路径和排除路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/v1/**") // 拦截所有 /api/v1 下的请求
                .excludePathPatterns(
                        "/api/v1/users/login",    // 登录不拦截
                        "/api/v1/users/register"  // 注册不拦截
                );
    }

    /**
     * 配置 CORS 跨域访问规则
     *
     * 前后端分离架构下，前端（localhost:5173）访问后端（localhost:8080）
     * 属于跨域请求，需要后端明确允许。
     *
     * allowCredentials(true) 是关键设置：
     * 后端需要通过 Cookie 传递 JSESSIONID 给前端，
     * 前端也需要通过 credentials: 'include' 携带 Cookie。
     * 如果 allowCredentials 为 false，浏览器不会发送 Cookie，Session 机制失效。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // 允许携带 Cookie（Session 鉴权必须开启）
    }
}
