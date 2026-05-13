package com.reins.bookstore.controller;

import com.reins.bookstore.entity.Book;
import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.entity.Order;
import com.reins.bookstore.entity.OrderItem;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.repository.CartRepository;
import com.reins.bookstore.repository.OrderItemRepository;
import com.reins.bookstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

    private Map<String, Object> toOrderResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<Map<String, Object>> responseItems = new ArrayList<>();
        double totalPrice = 0;
        int totalCount = 0;

        for (OrderItem item : items) {
            int quantity = item.getNumber() == null ? 0 : item.getNumber();
            int unitPrice = item.getUnitPrice() == null ? 0 : item.getUnitPrice();
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("bookId", item.getBookId());
            itemMap.put("title", item.getBookTitle());
            itemMap.put("cover", item.getBookCover());
            itemMap.put("number", quantity);
            itemMap.put("unitPrice", unitPrice);
            itemMap.put("price", unitPrice);
            responseItems.add(itemMap);

            totalCount += quantity;
            totalPrice += quantity * unitPrice / 100.0;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("address", order.getAddress());
        response.put("receiver", order.getReceiver());
        response.put("tel", order.getTel());
        response.put("userId", order.getUserId());
        response.put("createdAt", order.getCreatedAt());
        response.put("items", responseItems);
        response.put("totalCount", totalCount);
        response.put("totalPrice", totalPrice);
        return response;
    }

    /**
     * 查询特定订单详情
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(toOrderResponse(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询用户的所有订单
     */
    @GetMapping("/orders/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<Map<String, Object>> responseOrders = new ArrayList<>();
        for (Order order : orders) {
            responseOrders.add(toOrderResponse(order));
        }
        return ResponseEntity.ok(Map.of("orders", responseOrders));
    }

    /**
     * 创建订单
     */
    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.parseLong(params.get("userId").toString());

            //TODO: 这里还没实现，要和前端联动
            String receiver = params.get("receiver").toString();
            String address = params.get("address").toString();
            String tel = params.get("tel").toString();

            List<Cart> cartItems = cartRepository.findByUserId(userId);
            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setReceiver(receiver);
            order.setAddress(address);
            order.setTel(tel);
            order.setCreatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);
            logger.debug("Order saved: id={}", savedOrder.getId());

            for (Cart cartItem : cartItems) {
                Book book = bookRepository.findById(cartItem.getBookId()).orElse(null);
                if (book == null) {
                    continue;
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getId());
                orderItem.setBookId(book.getId());
                orderItem.setBookTitle(book.getTitle());
                orderItem.setBookCover(book.getCover());
                orderItem.setUnitPrice(book.getPrice() == null ? 0 : book.getPrice().intValue());
                orderItem.setNumber(cartItem.getNumber());
                orderItemRepository.save(orderItem);
            }

            logger.debug("Deleting {} cart items for user {}", cartItems.size(), userId);
            cartRepository.deleteByUserId(userId);
            logger.debug("Cart cleared for user {}", userId);

            Map<String, Object> response = toOrderResponse(savedOrder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("createOrder failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage() == null ? e.toString() : e.getMessage());
            err.put("path", "/api/v1/orders");
            err.put("status", 500);
            return ResponseEntity.status(500).body(err);
        }
    }
}
