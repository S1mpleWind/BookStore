package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserAuth 实体类映射数据库中的 user_auth 表
 * 采用鉴权信息与基本信息分离的设计，存储用户名、密码及身份标识
 */
@Entity
@Table(name = "user_auth")
@Data
@NoArgsConstructor
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; // 登录用户名
    private String password; // 登录密码
    private Integer identity; // 身份标识：0 为普通用户，1 为管理员

    @Column(name = "user_id", unique = true)
    private Long userId; // 关联的 User 实体 ID
    private Boolean enable = true; // 是否启用（禁用则无法登录）
}
