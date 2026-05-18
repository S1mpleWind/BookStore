package com.reins.bookstore.service.impl;

import com.reins.bookstore.entity.Book;
import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.entity.Order;
import com.reins.bookstore.entity.OrderItem;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.repository.CartRepository;
import com.reins.bookstore.repository.OrderItemRepository;
import com.reins.bookstore.repository.OrderRepository;
import com.reins.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderServiceImpl 负责订单数据的转换与业务逻辑处理
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CartRepository cartRepository;

    @Override
    public List<Map<String, Object>> findOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> findOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::toOrderResponse)
                .orElse(null);
    }

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

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long userId, String receiver, String address, String tel) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return Map.of("error", "购物车为空");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setReceiver(receiver);
        order.setAddress(address);
        order.setTel(tel);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

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

        // 清空购物车
        cartRepository.deleteByUserId(userId);

        return toOrderResponse(savedOrder);
    }
}
