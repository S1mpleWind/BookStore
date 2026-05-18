package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Book 实体类映射数据库中的 book 表
 * 包含了书籍的基本信息如标题、作者、价格等
 */
@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;    // 书名
    private String author;   // 作者
    private String cover;    // 封面图片 URL
    private Double price;    // 价格

    @Column(name = "description", columnDefinition = "TEXT")
    private String desc;     // 书籍描述

    private Boolean recommended = false; // 是否推荐
}
