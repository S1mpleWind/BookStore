package com.reins.bookstore.service.impl;

import com.reins.bookstore.entity.User;
import com.reins.bookstore.entity.UserAuth;
import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.repository.UserRepository;
import com.reins.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * UserServiceImpl 负责用户注册和登录的具体业务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Override
    @Transactional
    public Map<String, String> register(String username, String password, String nickname) {
        Map<String, String> res = new HashMap<>();
        
        if (userAuthRepository.findByUsername(username) != null) {
            res.put("error", "Username already exists");
            return res;
        }

        // 1. 保存基本信息
        User user = new User();
        user.setNickname(nickname != null ? nickname : username);
        user.setBalance(0L);
        User savedUser = userRepository.save(user);

        // 2. 保存认证信息
        UserAuth userAuth = new UserAuth();
        userAuth.setUsername(username);
        userAuth.setPassword(password);
        userAuth.setUserId(savedUser.getId());
        userAuth.setIdentity(0); 
        userAuthRepository.save(userAuth);

        res.put("message", "User registered successfully");
        return res;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> res = new HashMap<>();
        
        UserAuth userAuth = userAuthRepository.findByUsername(username);
        if (userAuth == null || !password.equals(userAuth.getPassword())) {
            res.put("error", "Invalid username or password");
            return res;
        }

        User user = userRepository.findById(userAuth.getUserId()).orElse(null);
        if (user == null) {
            res.put("error", "User profile not found");
            return res;
        }

        res.put("userId", user.getId());
        res.put("username", userAuth.getUsername());
        res.put("nickname", user.getNickname());
        res.put("identity", userAuth.getIdentity());
        return res;
    }
}
