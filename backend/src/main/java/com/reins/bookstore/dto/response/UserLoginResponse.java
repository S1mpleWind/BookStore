package com.reins.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserLoginResponse 是用户登录成功的响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    private Long userId;
    private String username;
    private String nickname;
    private Integer identity;
}