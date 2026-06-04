package com.reins.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderItemDTO 是订单项的响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long bookId;
    private String title;
    private String cover;
    private Integer number;
    private Integer unitPrice;
    private Integer price;       // 总价 = unitPrice * number
}