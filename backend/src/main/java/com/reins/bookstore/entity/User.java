package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity                 //映射成表
@Table(name = "user")   //表名
@Data
@NoArgsConstructor
public class User {
    @Id // "这是主键"
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private Long balance = 0L;
}
