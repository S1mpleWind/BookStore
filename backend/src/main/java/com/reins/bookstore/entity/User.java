package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User 实体类映射数据库中的 user 表
 * 存储用户的非敏感基本信息，如昵称和余额
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname; // 用户昵称
    private Long balance = 0L; // 账户余额（单位：分）
}
