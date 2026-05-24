package com.reins.bookstore.controller;

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
     * @param params 包含 username, password, nickname 的 JSON 对象
     * @return 注册成功或失败的消息
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Map<String, String> result = userService.register(username, password, nickname);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登录接口
     * @param params 包含 username, password 的 JSON 对象
     * @return 用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Map<String, Object> result = userService.login(username, password);
        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result);
        }

        // 登录成功，将用户信息存入 Session
        HttpSession session = request.getSession();
        session.setAttribute("userId", result.get("userId"));
        session.setAttribute("identity", result.get("identity"));

        return ResponseEntity.ok(result);
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
