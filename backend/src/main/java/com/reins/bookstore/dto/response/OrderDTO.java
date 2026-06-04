package com.reins.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderDTO 是订单的响应 DTO
 * 包含订单头信息和订单项列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String address;
    private String receiver;
    private String tel;
    private Long userId;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
    private Integer totalCount;
    private Double totalPrice;
}