package com.reins.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CartItemDTO 是购物车商品的响应 DTO
 * 包含商品的基本信息和书籍的详细信息（书名、封面、单价等）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long bookId;
    private Long userId;
    private Integer number;
    private String bookTitle;
    private String bookCover;
    private Integer unitPrice;
}