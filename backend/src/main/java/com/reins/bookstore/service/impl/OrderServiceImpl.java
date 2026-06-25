package com.reins.bookstore.service.impl;

import com.reins.bookstore.dto.response.OrderDTO;
import com.reins.bookstore.dto.response.OrderItemDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.entity.Order;
import com.reins.bookstore.entity.OrderItem;
import com.reins.bookstore.entity.User;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.repository.CartRepository;
import com.reins.bookstore.repository.OrderItemRepository;
import com.reins.bookstore.repository.OrderRepository;
import com.reins.bookstore.repository.UserRepository;
import com.reins.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private UserRepository userRepository;

    /**
     * 将 Order Entity 转换为 OrderDTO
     * 利用 JPA 关联关系：通过 order.getItems() 直接获取关联的 OrderItem 列表
     */
    private OrderDTO toOrderDTO(Order order) {
        List<OrderItem> items = order.getItems(); // 通过 JPA 关联直接从 Order 导航获取
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
                    unitPrice
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
                order.getUserId(),  // @Transient getter，通过 user 实体导航获取
                order.getCreatedAt(),
                itemDTOs,
                totalCount,
                totalPrice
        );
    }

    @Override
    public List<OrderDTO> findOrdersByUserId(Long userId) {
        return orderRepository.findByUser_Id(userId).stream()
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
        List<Cart> cartItems = cartRepository.findByUser_Id(userId);
        if (cartItems.isEmpty()) {
            return null;
        }

        // 查找用户实体以建立 JPA 关联
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Order order = new Order();
        order.setUser(user);       // 通过 JPA 关联设置用户
        order.setReceiver(receiver);
        order.setAddress(address);
        order.setTel(tel);
        order.setCreatedAt(LocalDateTime.now());

        // 第一步：校验库存是否足够
        for (Cart cartItem : cartItems) {
            Book book = bookRepository.findById(cartItem.getBookId()).orElse(null);
            if (book == null) continue;
            int orderedQty = cartItem.getNumber() != null ? cartItem.getNumber() : 0;
            int currentInventory = book.getInventory() != null ? book.getInventory() : 0;
            if (currentInventory < orderedQty) {
                throw new RuntimeException("《" + book.getTitle() + "》库存不足，当前库存：" + currentInventory + "，需要：" + orderedQty);
            }
        }

        // 第二步：生成订单项（通过 JPA 级联关联，保存 Order 时会自动保存 OrderItem）
        for (Cart cartItem : cartItems) {
            Book book = bookRepository.findById(cartItem.getBookId()).orElse(null);
            if (book == null) continue;

            int orderedQty = cartItem.getNumber() != null ? cartItem.getNumber() : 0;
            book.setInventory(book.getInventory() - orderedQty);
            book.setSales((book.getSales() != null ? book.getSales() : 0) + orderedQty);
            bookRepository.save(book);

            OrderItem orderItem = new OrderItem();
            orderItem.setBookId(book.getId());
            orderItem.setBookTitle(book.getTitle());
            orderItem.setBookCover(book.getCover());
            orderItem.setUnitPrice(book.getPrice() == null ? 0 : book.getPrice().intValue());
            orderItem.setNumber(orderedQty);

            // 通过 JPA 双向关联添加，利用 cascade = ALL 在保存 Order 时级联保存 OrderItem
            order.addItem(orderItem);
        }

        // 保存 Order（级联保存 OrderItem）
        Order savedOrder = orderRepository.save(order);

        // 清空购物车
        cartRepository.deleteByUser_Id(userId);

        return toOrderDTO(savedOrder);
    }

    @Override
    public List<OrderDTO> findAllOrders() {
        return orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> searchOrders(Long userId, LocalDateTime start, LocalDateTime end, String bookTitle) {
        List<Order> orders;
        if (userId != null && start != null && end != null) {
            orders = orderRepository.findByUserIdAndTimeRange(userId, start, end);
        } else if (userId != null && start != null) {
            orders = orderRepository.findByUserIdAndTimeRange(userId, start, LocalDateTime.now());
        } else if (userId != null) {
            orders = orderRepository.findByUser_Id(userId);
        } else if (start != null && end != null) {
            orders = orderRepository.findByTimeRange(start, end);
        } else {
            orders = orderRepository.findAll();
        }

        // 按时间倒序
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        List<OrderDTO> dtos = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO dto = toOrderDTO(order);
            // 如果指定了书名，过滤
            if (bookTitle != null && !bookTitle.trim().isEmpty()) {
                boolean match = dto.getItems() != null && dto.getItems().stream()
                        .anyMatch(i -> i.getTitle() != null &&
                                i.getTitle().toLowerCase().contains(bookTitle.toLowerCase()));
                if (!match) continue;
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<OrderDTO> searchAllOrders(LocalDateTime start, LocalDateTime end, String bookTitle) {
        return searchOrders(null, start, end, bookTitle);
    }

    @Override
    public List<Map<String, Object>> getSalesRanking(LocalDateTime start, LocalDateTime end) {
        List<Order> orders;
        if (start != null && end != null) {
            orders = orderRepository.findByTimeRange(start, end);
        } else {
            orders = orderRepository.findAll();
        }

        Map<Long, Integer[]> bookSales = new LinkedHashMap<>();
        Map<Long, String> bookInfo = new HashMap<>();

        for (Order order : orders) {
            List<OrderItem> items = order.getItems(); // 通过 JPA 关联导航获取
            for (OrderItem item : items) {
                if (item.getBookId() == null) continue;
                Integer[] arr = bookSales.computeIfAbsent(item.getBookId(), k -> new Integer[]{0});
                arr[0] += item.getNumber() != null ? item.getNumber() : 0;
                bookInfo.putIfAbsent(item.getBookId(), item.getBookTitle());
            }
        }

        return bookSales.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0].compareTo(a.getValue()[0]))
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bookId", e.getKey());
                    m.put("bookTitle", bookInfo.get(e.getKey()));
                    m.put("sales", e.getValue()[0]);
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getConsumptionRanking(LocalDateTime start, LocalDateTime end) {
        List<Order> orders;
        if (start != null && end != null) {
            orders = orderRepository.findByTimeRange(start, end);
        } else {
            orders = orderRepository.findAll();
        }

        Map<Long, Long> userSpending = new LinkedHashMap<>();
        Map<Long, String> userNames = new HashMap<>();

        for (Order order : orders) {
            if (order.getUserId() == null) continue;
            List<OrderItem> items = order.getItems(); // 通过 JPA 关联导航获取
            long total = 0;
            for (OrderItem item : items) {
                int qty = item.getNumber() != null ? item.getNumber() : 0;
                int price = item.getUnitPrice() != null ? item.getUnitPrice() : 0;
                total += (long) qty * price;
            }
            userSpending.merge(order.getUserId(), total, Long::sum);
            userNames.putIfAbsent(order.getUserId(), order.getReceiver() != null ? order.getReceiver() : "用户" + order.getUserId());
        }

        return userSpending.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", e.getKey());
                    m.put("nickname", userNames.get(e.getKey()));
                    m.put("totalAmount", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPersonalStatistics(Long userId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Order> orders;

        if (start != null && end != null) {
            orders = orderRepository.findByUserIdAndTimeRange(userId, start, end);
        } else {
            orders = orderRepository.findByUser_Id(userId);
        }

        Map<Long, Integer> bookCount = new LinkedHashMap<>();
        Map<Long, String> bookNames = new HashMap<>();
        int totalBooks = 0;
        long totalAmount = 0;

        for (Order order : orders) {
            List<OrderItem> items = order.getItems(); // 通过 JPA 关联导航获取
            for (OrderItem item : items) {
                int qty = item.getNumber() != null ? item.getNumber() : 0;
                int price = item.getUnitPrice() != null ? item.getUnitPrice() : 0;
                bookCount.merge(item.getBookId(), qty, Integer::sum);
                bookNames.putIfAbsent(item.getBookId(), item.getBookTitle());
                totalBooks += qty;
                totalAmount += (long) qty * price;
            }
        }

        List<Map<String, Object>> details = bookCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bookId", e.getKey());
                    m.put("bookTitle", bookNames.get(e.getKey()));
                    m.put("quantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        result.put("details", details);
        result.put("totalBooks", totalBooks);
        result.put("totalAmount", totalAmount);
        return result;
    }
}