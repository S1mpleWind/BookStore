package com.reins.bookstore.service.impl;

import com.reins.bookstore.dto.response.OrderDTO;
import com.reins.bookstore.dto.response.OrderItemDTO;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderServiceImpl 负责订单数据的转换与业务逻辑处理
 * 使用 OrderDTO / OrderItemDTO 替代 Map<String, Object>，保证类型安全
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

    /**
     * 将 Order Entity + 关联的 OrderItems 转换为 OrderDTO
     */
    private OrderDTO toOrderDTO(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        double totalPrice = 0;
        int totalCount = 0;

        for (OrderItem item : items) {
            int quantity = item.getNumber() == null ? 0 : item.getNumber();
            int unitPrice = item.getUnitPrice() == null ? 0 : item.getUnitPrice();

            OrderItemDTO itemDTO = new OrderItemDTO(
                    item.getId(),
                    item.getBookId(),
                    item.getBookTitle(),
                    item.getBookCover(),
                    quantity,
                    unitPrice,
                    unitPrice  // price 字段保持与之前接口兼容
            );
            itemDTOs.add(itemDTO);

            totalCount += quantity;
            totalPrice += quantity * unitPrice / 100.0;
        }

        return new OrderDTO(
                order.getId(),
                order.getAddress(),
                order.getReceiver(),
                order.getTel(),
                order.getUserId(),
                order.getCreatedAt(),
                itemDTOs,
                totalCount,
                totalPrice
        );
    }

    @Override
    public List<OrderDTO> findOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO findOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::toOrderDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, String receiver, String address, String tel) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return null;
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

        return toOrderDTO(savedOrder);
    }
}