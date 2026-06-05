package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.UserLoginResponse;
import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> register(String username, String password, String confirmPassword, String nickname, String email);
    UserLoginResponse login(String username, String password);
    List<Map<String, Object>> listAllUsers();
    Map<String, Object> toggleUserStatus(Long userId);
}