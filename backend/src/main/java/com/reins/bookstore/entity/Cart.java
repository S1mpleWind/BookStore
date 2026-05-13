package com.reins.bookstore.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "user_id")
    private Long userId;
}