package com.reins.bookstore.controller;

import com.reins.bookstore.dto.request.UserLoginRequest;
import com.reins.bookstore.dto.request.UserRegisterRequest;
import com.reins.bookstore.dto.response.UserLoginResponse;
import com.reins.bookstore.dto.response.TokenLoginResponse;
import com.reins.bookstore.security.JwtService;
import com.reins.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }
        Map<String, Object> result = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getConfirmPassword(),
                request.getNickname(),
                request.getEmail());
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }
        if (request.getUsername().trim().isEmpty() || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }

        //* 调用 service，检查是否能登录
        UserLoginResponse user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        // 检查是否被禁用：如果 identity 被特殊标记 -1
        if (user.getIdentity() == -1) {
            return ResponseEntity.status(403).body(Map.of("error", "您的账号已经被禁用"));
        }

        String token = jwtService.generate(user.getUserId(), user.getIdentity());
        return ResponseEntity.ok(TokenLoginResponse.of(user, token, jwtService.getTtlSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        // 客户端清除 JWT；已经签发的凭证在过期前仍然有效。
        return ResponseEntity.ok(Map.of("message", "已登出"));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listAllUsers(HttpServletRequest request) {
        if (request.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Integer identity = (Integer) request.getAttribute("identity");
        if (identity == null || identity != 1) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, HttpServletRequest request) {
        if (request.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Integer identity = (Integer) request.getAttribute("identity");
        if (identity == null || identity != 1) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        return ResponseEntity.ok(userService.toggleUserStatus(userId));
    }
}