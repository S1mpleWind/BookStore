package com.reins.bookstore.service.impl;

import com.reins.bookstore.dto.response.UserLoginResponse;
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
    public UserLoginResponse login(String username, String password) {
        UserAuth userAuth = userAuthRepository.findByUsername(username);
        if (userAuth == null || !password.equals(userAuth.getPassword())) {
            return null;
        }

        User user = userRepository.findById(userAuth.getUserId()).orElse(null);
        if (user == null) {
            return null;
        }

        return new UserLoginResponse(
                user.getId(),
                userAuth.getUsername(),
                user.getNickname(),
                userAuth.getIdentity()
        );
    }
}
