package com.reins.bookstore.service;

import com.reins.bookstore.entity.User;
import java.util.Map;

/**
 * UserService 接口定义了用户相关的业务逻辑
 */
public interface UserService {
    /**
     * 用户注册逻辑
     */
    Map<String, String> register(String username, String password, String nickname);

    /**
     * 用户登录逻辑
     */
    Map<String, Object> login(String username, String password);
}
