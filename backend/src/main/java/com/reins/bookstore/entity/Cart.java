package com.reins.bookstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cart 实体类映射数据库中的 cart_item 表
 * 记录用户购物车中的商品项
 * 与 User、Book 均为多对一关联关系
 */
@Entity
@Table(name = "cart_item")
@Data
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private Integer number;

    /**
     * 多对一关联：多个购物车项从属于同一个用户
     * 外键列名为 user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    /**
     * 多对一关联：多个购物车项指向同一本书
     * 外键列名为 book_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private Book book;

    /**
     * 便捷获取用户 ID（通过关联实体导航）
     */
    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * 便捷获取书籍 ID（通过关联实体导航）
     */
    @Transient
    public Long getBookId() {
        return book != null ? book.getId() : null;
    }
}