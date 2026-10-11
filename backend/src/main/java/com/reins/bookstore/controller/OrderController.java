package com.reins.bookstore.controller;

import com.reins.bookstore.dto.response.OrderDTO;
import com.reins.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, HttpServletRequest request) {
        OrderDTO order = orderService.findOrderById(id);
        if (order == null) return ResponseEntity.notFound().build();
        if (!isAdmin(request) && !order.getUserId().equals(request.getAttribute("userId"))) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String bookTitle,
            HttpServletRequest request) {
        if (request.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        // 从验证后的 JWT 请求身份获取当前用户 ID（防越权：用户只能查自己的订单）
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.searchOrders(userId, start, end, bookTitle));
    }

    @GetMapping("/orders/all")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String bookTitle,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(orderService.searchAllOrders(start, end, bookTitle));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String receiver = params.get("receiver").toString();
            String address = params.get("address").toString();
            String tel = params.get("tel").toString();
            OrderDTO result = orderService.createOrder(userId, receiver, address, tel);
            if (result == null) return ResponseEntity.badRequest().body(Map.of("error", "购物车为空"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/statistics/sales")
    public ResponseEntity<?> getSalesRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(orderService.getSalesRanking(start, end));
    }

    @GetMapping("/statistics/consumption")
    public ResponseEntity<?> getConsumptionRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(orderService.getConsumptionRanking(start, end));
    }

    @GetMapping("/statistics/my-purchases")
    public ResponseEntity<?> getMyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        if (request.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.getPersonalStatistics(userId, start, end));
    }

    private boolean isAdmin(HttpServletRequest request) {
        Integer identity = (Integer) request.getAttribute("identity");
        return identity != null && identity == 1;
    }
}