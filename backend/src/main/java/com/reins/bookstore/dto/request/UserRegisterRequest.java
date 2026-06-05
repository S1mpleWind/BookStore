package com.reins.bookstore.dto.request;

import lombok.Data;

/**
 * UserRegisterRequest 是用户注册的请求 DTO
 */
@Data
public class UserRegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String email;
}