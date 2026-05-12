package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String cover;
    private Double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String desc; // 注意这里保持和前端 json 中的 "desc" 一致

    private Boolean recommended = false;
}
