package com.reins.bookstore.controller;

import com.reins.bookstore.dto.request.UserLoginRequest;
import com.reins.bookstore.dto.request.UserRegisterRequest;
import com.reins.bookstore.dto.response.UserLoginResponse;
import com.reins.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

/**
 * UserController 负责处理与用户相关的 HTTP 请求
 * 包括用户注册和登录功能
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * @param request 包含 username, password, nickname 的请求 DTO
     * @return 注册成功或失败的消息
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Map<String, String> result = userService.register(
                request.getUsername(), request.getPassword(), request.getNickname());
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登录接口
     * @param request 包含 username, password 的请求 DTO
     * @return 登录成功返回用户信息，失败返回 401
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request, HttpServletRequest servletRequest) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        UserLoginResponse user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        // 登录成功，将用户信息存入 Session
        HttpSession session = servletRequest.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("identity", user.getIdentity());

        return ResponseEntity.ok(user);
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
