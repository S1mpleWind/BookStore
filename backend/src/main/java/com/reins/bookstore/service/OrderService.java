package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.OrderDTO;
import java.util.List;

/**
 * OrderService 接口定义了订单相关的业务逻辑
 * 返回 OrderDTO 而非 Map，保证类型安全
 */
public interface OrderService {
    /**
     * 获取用户所有订单及其详情
     */
    List<OrderDTO> findOrdersByUserId(Long userId);

    /**
     * 获取单笔订单详情
     */
    OrderDTO findOrderById(Long id);

    /**
     * 创建订单
     */
    OrderDTO createOrder(Long userId, String receiver, String address, String tel);
}
