package com.reins.bookstore.service;

import com.reins.bookstore.entity.Order;
import java.util.List;
import java.util.Map;

/**
 * OrderService 接口定义了订单相关的业务逻辑
 */
public interface OrderService {
    /**
     * 获取用户所有订单及其详情
     */
    List<Map<String, Object>> findOrdersByUserId(Long userId);

    /**
     * 获取单笔订单详情
     */
    Map<String, Object> findOrderById(Long id);

    /**
     * 创建订单
     */
    Map<String, Object> createOrder(Long userId, String receiver, String address, String tel);
}
