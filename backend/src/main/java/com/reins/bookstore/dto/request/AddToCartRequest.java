package com.reins.bookstore.dto.request;

import lombok.Data;

/**
 * AddToCartRequest 是添加购物车的请求 DTO
 */
@Data
public class AddToCartRequest {
    private Long userId;
    private Long bookId;
    private Integer quantity;
}