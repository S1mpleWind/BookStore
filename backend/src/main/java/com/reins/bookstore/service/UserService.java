package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.UserLoginResponse;

import java.util.Map;

/**
 * UserService 接口定义了用户相关的业务逻辑
 */
public interface UserService {
    /**
     * 用户注册逻辑
     * @return 包含 message 或 error 的 Map（简单场景暂不提取 DTO）
     */
    Map<String, String> register(String username, String password, String nickname);

    /**
     * 用户登录逻辑
     * @return 登录成功返回 UserLoginResponse，失败返回 null
     */
    UserLoginResponse login(String username, String password);
}
