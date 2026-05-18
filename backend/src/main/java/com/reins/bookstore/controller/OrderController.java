package com.reins.bookstore.controller;

import com.reins.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OrderController 负责处理订单相关的 HTTP 请求
 * 包括查询订单列表和查询订单详情
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {


    @Autowired
    private OrderService orderService;

    /**
     * 根据订单 ID 查询特定订单详情
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Object order = orderService.findOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 查询用户的所有订单
     */
    @GetMapping("/orders/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findOrdersByUserId(userId));
    }

    /**
     * 创建订单
     * @param params 包含 userId, receiver, address, tel 的 JSON 对象
     */
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.parseLong(params.get("userId").toString());
            String receiver = params.get("receiver").toString();
            String address = params.get("address").toString();
            String tel = params.get("tel").toString();

            Map<String, Object> result = orderService.createOrder(userId, receiver, address, tel);
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

