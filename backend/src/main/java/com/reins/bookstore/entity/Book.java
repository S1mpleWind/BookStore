package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book")
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
    private String desc;
    private Integer inventory = 100;
    private String publisher;
    private String isbn;
    private Integer sales = 0;

    private Boolean recommended = false;
}