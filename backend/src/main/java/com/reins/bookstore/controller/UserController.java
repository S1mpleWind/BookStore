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
 * 用户控制器
 * 处理用户注册、登录、登出以及管理员对用户的管理操作。
 *
 * 采用 Session 鉴权机制：
 * - 登录成功后后端创建 HttpSession，存 userId 和 identity
 * - 后续请求通过 SessionInterceptor 校验 Session 是否存在
 * - 管理员操作额外校验 identity == 1
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * 请求体包含 username、password、confirmPassword、nickname、email。
     * 后端进行字段校验：
     * - 用户名/密码不能为空
     * - 两次密码必须一致
     * - 邮箱不能为空且须符合格式
     * - 用户名不能重复
     *
     * @param request 注册请求体 DTO
     * @return 注册成功返回 { message: "注册成功" }，失败返回 { error: "错误信息" }
     */
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

    /**
     * 用户登录
     *
     * 登录流程：
     * 1. 校验用户名、密码不为空（前后端双重校验）
     * 2. 调用 UserService.login() 验证身份
     * 3. 若返回 null → 用户名或密码错误，返回 401
     * 4. 若 identity == -1 → 账号被禁用，返回 403 + 特殊提示
     * 5. 登录成功 → 创建 HttpSession，存入 userId 和 identity
     *
     * @param request        登录请求（username + password）
     * @param servletRequest 用于获取/创建 Session
     * @return 成功返回 UserLoginResponse（userId, username, nickname, identity）
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request, HttpServletRequest servletRequest) {
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

        // 登录成功，创建 Session 保存用户身份
        HttpSession session = servletRequest.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("identity", user.getIdentity());

        return ResponseEntity.ok(user);
    }

    /**
     * 用户登出
     * 使当前 Session 失效，清除服务器端的登录状态。
     */
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
     *
     * 权限校验：
     * - 必须已登录（Session 中有 userId）
     * - 必须是管理员（identity == 1）
     *
     * @return 用户列表，每个用户包含 id, userId, username, nickname, email, identity, enable
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
     *
     * 切换用户的 enable 状态：启用→禁用，禁用→启用。
     * 管理员不能禁用自己（前端按钮 disabled）。
     *
     * @param userId 要操作的目标用户 ID
     * @return { message: "用户已禁用/用户已启用", enable: true/false }
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