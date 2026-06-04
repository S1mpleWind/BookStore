package com.reins.bookstore.dto.request;

import lombok.Data;

/**
 * UserLoginRequest 是用户登录的请求 DTO
 */
@Data
public class UserLoginRequest {
    private String username;
    private String password;
}