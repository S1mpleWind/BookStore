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

/**
 * 订单与统计控制器
 *
 * 处理订单的创建、查询、搜索，以及统计数据（热销榜、消费榜、个人统计）。
 *
 * 权限划分：
 * - 顾客：查看自己的订单 / 个人统计
 * - 管理员：查看所有订单 / 热销榜 / 消费榜
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 查询单个订单详情（含订单项明细）
     *
     * @param id 订单 ID
     * @return OrderDTO（含 items 列表）
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.findOrderById(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(order);
    }

    /**
     * 顾客：查询自己的订单（支持多条件搜索过滤）
     *
     * 搜索条件（可组合使用）：
     * - start + end：时间范围
     * - bookTitle：按书名过滤
     *
     * @param start     起始时间（ISO 格式，可选）
     * @param end       结束时间（ISO 格式，可选）
     * @param bookTitle 书名关键词（可选）
     * @param request   用于从 Session 获取当前用户 ID
     * @return List<OrderDTO>
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
        // 从 Session 获取当前用户 ID（防越权：用户只能查自己的订单）
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(orderService.searchOrders(userId, start, end, bookTitle));
    }

    /**
     * 管理员：查看所有订单（支持搜索过滤）
     *
     * 搜索条件同 getMyOrders，但返回所有用户的订单。
     *
     * @param request 用于校验管理员身份
     * @return List<OrderDTO>
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

    /**
     * 创建订单（从购物车生成订单）
     *
     * 业务流程：
     * 1. 获取用户购物车中的所有商品
     * 2. 校验每本书的库存是否充足
     * 3. 扣减库存、增加销量
     * 4. 生成订单和订单项（通过 JPA cascade 级联保存）
     * 5. 清空购物车
     *
     * 整个操作在 @Transactional 事务中执行，保证数据一致性。
     *
     * @param params { userId, receiver, address, tel }
     * @return OrderDTO 或 { error: "购物车为空" }
     */
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
     * 管理员：热销榜——统计指定时间范围内每本书的销量，按销量降序排列
     *
     * @param start   起始时间（可选）
     * @param end     结束时间（可选）
     * @param request 用于校验管理员身份
     * @return [{ bookId, bookTitle, sales }, ...]
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
     * 管理员：消费榜——统计指定时间范围内每个用户的总消费金额，按金额降序排列
     *
     * @param start   起始时间（可选）
     * @param end     结束时间（可选）
     * @param request 用于校验管理员身份
     * @return [{ userId, nickname, totalAmount }, ...]
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
     * 顾客：个人统计——查看自己指定时间范围内的购书记录
     *
     * @param start   起始时间（可选）
     * @param end     结束时间（可选）
     * @param request 用于从 Session 获取当前用户 ID
     * @return { details: [{ bookId, bookTitle, quantity }], totalBooks, totalAmount }
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

    /**
     * 校验当前用户是否为管理员
     *
     * @param request HTTP 请求
     * @return true 如果是管理员（identity == 1）
     */
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer identity = (Integer) session.getAttribute("identity");
        return identity != null && identity == 1;
    }
}