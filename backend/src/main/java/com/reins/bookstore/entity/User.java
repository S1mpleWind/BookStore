package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * User 实体类映射数据库中的 user 表
 * 存储用户的非敏感基本信息，如昵称和余额
 * 与 Order、Cart 为一对多关联关系
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
    private String email;    // 邮箱

    /**
     * 一对多关联：一个用户可以有多个订单
     * mappedBy = "user"：由 Order 中的 user 字段维护关联
     * cascade = CascadeType.ALL：用户删除时级联删除关联的订单
     * fetch = LAZY：延迟加载
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    /**
     * 一对多关联：一个用户可以有多个购物车项
     * mappedBy = "user"：由 Cart 中的 user 字段维护关联
     * cascade = CascadeType.ALL：用户删除时级联删除购物车
     * fetch = LAZY：延迟加载
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Cart> cartItems = new ArrayList<>();
}