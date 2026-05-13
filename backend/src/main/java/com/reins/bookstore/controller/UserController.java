package com.reins.bookstore.controller;

import com.reins.bookstore.entity.User;
import com.reins.bookstore.entity.UserAuth;
import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// controller + responce body 返回值都会自动序列化成 JSON，直接写入 HTTP 响应体返回给前端
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @PostMapping("/register")
    @Transactional // 保证 User 和 UserAuth 同时写入数据库，要么都成功要么都失败
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        if (userAuthRepository.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // 1. 创建并保存基本用户信息
        User user = new User();
        user.setNickname(nickname != null ? nickname : username);
        user.setBalance(0L);
        User savedUser = userRepository.save(user);

        // 2. 创建并保存认证信息
        UserAuth userAuth = new UserAuth();
        userAuth.setUsername(username);
        userAuth.setPassword(password);
        userAuth.setUserId(savedUser.getId());
        userAuth.setIdentity(0); // 默认普通用户
        userAuthRepository.save(userAuth);

        return ResponseEntity.ok("User registered successfully");
    }
}
