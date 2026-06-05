package com.reins.bookstore.controller;

import com.reins.bookstore.dto.response.OrderDTO;
import com.reins.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.findOrderById(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(order);
    }

    /**
     * 顾客：查询自己的订单（支持时间范围 + 书名搜索）
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String bookTitle,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(orderService.searchOrders(userId, start, end, bookTitle));
    }

    /**
     * 管理员：查看所有订单（支持搜索）
     */
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
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.parseLong(params.get("userId").toString());
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

    /**
     * 热销榜
     */
    @GetMapping("/statistics/sales")
    public ResponseEntity<?> getSalesRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(orderService.getSalesRanking(start, end));
    }

    /**
     * 消费榜
     */
    @GetMapping("/statistics/consumption")
    public ResponseEntity<?> getConsumptionRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(orderService.getConsumptionRanking(start, end));
    }

    /**
     * 个人统计
     */
    @GetMapping("/statistics/my-purchases")
    public ResponseEntity<?> getMyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(orderService.getPersonalStatistics(userId, start, end));
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer identity = (Integer) session.getAttribute("identity");
        return identity != null && identity == 1;
    }
}