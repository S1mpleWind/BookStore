package com.reins.bookstore.controller;

import com.reins.bookstore.dto.request.AddToCartRequest;
import com.reins.bookstore.dto.response.CartItemDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.entity.User;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.repository.CartRepository;
import com.reins.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车控制器
 * 处理购物车的增删改查操作。
 *
 * 购物车数据存储在 MySQL 数据库的 cart_item 表中，
 * 通过 JPA 实体关联（Cart → User, Cart → Book）来维护关系，
 * 而不是直接操作外键 ID。
 */
@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 将 Cart Entity 转换为 CartItemDTO
     *
     * 利用 JPA 关联关系（cart.getBook()）直接从实体导航获取书籍信息，
     * 无需额外的数据库查询。体现了 ORM 关联导航的便利性。
     *
     * @param cart Cart 实体
     * @return CartItemDTO（含 bookId, userId, 数量, 书名, 封面, 单价）
     */
    private CartItemDTO toDTO(Cart cart) {
        // 通过 JPA 关联导航：cart.getBook() 直接拿到 Book 实体
        Book book = cart.getBook();
        return new CartItemDTO(
                cart.getId(),
                cart.getBookId(),       // @Transient 通过 book 实体导航获取
                cart.getUserId(),       // @Transient 通过 user 实体导航获取
                cart.getNumber(),
                book != null ? book.getTitle() : null,
                book != null ? book.getCover() : null,
                book != null && book.getPrice() != null ? book.getPrice().intValue() : 0
        );
    }

    /**
     * 查询用户的购物车
     *
     * 通过 JPA 关联路径 findByUser_Id() 查询指定用户的所有购物车项，
     * 并转换为 DTO 返回。
     *
     * @param userId 用户 ID
     * @return { items: [CartItemDTO, ...] }
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        List<CartItemDTO> dtos = cartRepository.findByUser_Id(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("items", dtos));
    }

    /**
     * 添加商品到购物车
     *
     * 使用 JPA 实体关联方式：
     * 1. 通过 userRepository 和 bookRepository 找到 User 和 Book 实体
     * 2. 检查购物车是否已有该商品——有则增加数量，无则新建 Cart 项
     * 3. 设置实体关联 cart.setUser(user)、cart.setBook(book)
     *
     * 注意：这里用的是实体关联而不是直接存 userId + bookId，
     * 体现了 JPA 操作对象而非操作外键的思想。
     *
     * @param request { userId, bookId, quantity }
     * @return { message: "Item added to cart" }
     */
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        Long userId = request.getUserId();
        Long bookId = request.getBookId();
        Integer quantity = request.getQuantity();

        // 查找 JPA 关联实体（不是直接操作外键 ID，而是通过实体建立关联）
        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);
        if (user == null || book == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户或书籍不存在"));
        }

        // 检查购物车中是否已有此商品
        // 通过 cart.getBook() 实体导航来比较，而非比较 bookId 字段
        Cart existingItem = cartRepository.findByUser_Id(userId).stream()
                .filter(item -> item.getBook() != null && item.getBook().getId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // 已有则增加数量
            existingItem.setNumber(existingItem.getNumber() + quantity);
            cartRepository.save(existingItem);
        } else {
            // 没有则新建购物车项，通过 JPA 实体关联建立关系
            Cart cartItem = new Cart();
            cartItem.setUser(user);   // 设置实体关联（JPA 自动维护外键）
            cartItem.setBook(book);   // 设置实体关联（JPA 自动维护外键）
            cartItem.setNumber(quantity);
            cartRepository.save(cartItem);
        }

        return ResponseEntity.ok(Map.of("message", "Item added to cart"));
    }

    /**
     * 更新购物车项的数量
     *
     * @param cartItemId 购物车项 ID
     * @param params     { quantity: 新数量 }
     * @return { message: "Cart item updated" } 或 404
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @RequestBody Map<String, Integer> params) {
        Integer quantity = params.get("quantity");

        return cartRepository.findById(cartItemId)
                .map(item -> {
                    item.setNumber(quantity);
                    cartRepository.save(item);
                    return ResponseEntity.ok(Map.of("message", "Cart item updated"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除购物车项
     *
     * @param cartItemId 要删除的购物车项 ID
     * @return { message: "Cart item deleted" } 或 404
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long cartItemId) {
        if (cartRepository.existsById(cartItemId)) {
            cartRepository.deleteById(cartItemId);
            return ResponseEntity.ok(Map.of("message", "Cart item deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 清空指定用户的所有购物车
     *
     * 在下单成功后调用，删除该用户购物车中的所有商品。
     * 注意：OrderServiceImpl.createOrder() 中下单时也会自动清空购物车，
     * 前端调用此接口是双重保障。
     *
     * @param userId 用户 ID
     * @return { message: "Cart cleared" }
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        cartRepository.deleteByUser_Id(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}