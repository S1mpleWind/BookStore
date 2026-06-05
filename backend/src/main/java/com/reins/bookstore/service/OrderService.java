package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.OrderDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    List<OrderDTO> findOrdersByUserId(Long userId);
    OrderDTO findOrderById(Long id);
    OrderDTO createOrder(Long userId, String receiver, String address, String tel);

    // 管理员查看所有订单
    List<OrderDTO> findAllOrders();

    // 搜索订单：支持时间范围 + 书名过滤
    List<OrderDTO> searchOrders(Long userId, LocalDateTime start, LocalDateTime end, String bookTitle);

    // 管理员搜索所有订单
    List<OrderDTO> searchAllOrders(LocalDateTime start, LocalDateTime end, String bookTitle);

    // 统计：热销榜（按销量排序）
    List<Map<String, Object>> getSalesRanking(LocalDateTime start, LocalDateTime end);

    // 统计：消费榜（按金额排序）
    List<Map<String, Object>> getConsumptionRanking(LocalDateTime start, LocalDateTime end);

    // 统计：个人购买统计
    Map<String, Object> getPersonalStatistics(Long userId, LocalDateTime start, LocalDateTime end);
}