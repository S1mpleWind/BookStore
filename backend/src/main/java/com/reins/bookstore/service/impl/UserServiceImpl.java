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

import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    @Transactional
    public Map<String, Object> register(String username, String password, String confirmPassword, String nickname, String email) {
        Map<String, Object> res = new HashMap<>();

        // 校验用户名是否为空
        if (username == null || username.trim().isEmpty()) {
            res.put("error", "用户名不能为空");
            return res;
        }
        // 校验密码是否为空
        if (password == null || password.isEmpty()) {
            res.put("error", "密码不能为空");
            return res;
        }
        // 校验两次密码是否一致
        if (!password.equals(confirmPassword)) {
            res.put("error", "两次输入的密码不一致");
            return res;
        }
        // 校验邮箱不能为空
        if (email == null || email.trim().isEmpty()) {
            res.put("error", "邮箱不能为空");
            return res;
        }
        // 校验邮箱格式
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            res.put("error", "邮箱格式不正确");
            return res;
        }
        // 校验用户名是否重复
        if (userAuthRepository.findByUsername(username) != null) {
            res.put("error", "用户名已存在");
            return res;
        }

        User user = new User();
        user.setNickname(nickname != null ? nickname : username);
        user.setBalance(0L);
        user.setEmail(email);
        User savedUser = userRepository.save(user);

        UserAuth userAuth = new UserAuth();
        userAuth.setUsername(username);
        userAuth.setPassword(password);
        userAuth.setUserId(savedUser.getId());
        userAuth.setIdentity(0);
        userAuth.setEnable(true);
        userAuthRepository.save(userAuth);

        res.put("message", "注册成功");
        return res;
    }

    @Override
    public UserLoginResponse login(String username, String password) {
        UserAuth userAuth = userAuthRepository.findByUsername(username);
        if (userAuth == null || !password.equals(userAuth.getPassword())) {
            return null;
        }

        // 检查用户是否被禁用 - 返回 identity=-1 让 Controller 区分
        if (userAuth.getEnable() != null && !userAuth.getEnable()) {
            return new UserLoginResponse(
                    userAuth.getUserId(),
                    userAuth.getUsername(),
                    "",
                    -1  // -1 表示被禁用
            );
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

    @Override
    public List<Map<String, Object>> listAllUsers() {
        List<UserAuth> auths = userAuthRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserAuth auth : auths) {
            User user = userRepository.findById(auth.getUserId()).orElse(null);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", auth.getId());
            map.put("userId", auth.getUserId());
            map.put("username", auth.getUsername());
            map.put("nickname", user != null ? user.getNickname() : "");
            map.put("email", user != null ? user.getEmail() : "");
            map.put("identity", auth.getIdentity());
            map.put("enable", auth.getEnable());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleUserStatus(Long userId) {
        Map<String, Object> res = new HashMap<>();
        UserAuth auth = userAuthRepository.findByUserId(userId);
        if (auth == null) {
            res.put("error", "用户不存在");
            return res;
        }
        boolean newStatus = auth.getEnable() == null || !auth.getEnable();
        auth.setEnable(newStatus);
        userAuthRepository.save(auth);
        res.put("message", newStatus ? "用户已启用" : "用户已禁用");
        res.put("enable", newStatus);
        return res;
    }
}