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

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

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
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request, HttpServletRequest servletRequest) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }
        if (request.getUsername().trim().isEmpty() || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }

        UserLoginResponse user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        // 检查是否被禁用：如果 identity 被特殊标记 -1
        if (user.getIdentity() == -1) {
            return ResponseEntity.status(403).body(Map.of("error", "您的账号已经被禁用"));
        }

        HttpSession session = servletRequest.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("identity", user.getIdentity());

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "已登出"));
    }

    /**
     * 管理员：获取所有用户列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> listAllUsers(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Integer identity = (Integer) session.getAttribute("identity");
        if (identity == null || identity != 1) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        return ResponseEntity.ok(userService.listAllUsers());
    }

    /**
     * 管理员：禁用/解禁用户
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Integer identity = (Integer) session.getAttribute("identity");
        if (identity == null || identity != 1) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        return ResponseEntity.ok(userService.toggleUserStatus(userId));
    }
}