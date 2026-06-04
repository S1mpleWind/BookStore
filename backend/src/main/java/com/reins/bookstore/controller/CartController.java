package com.reins.bookstore.controller;

import com.reins.bookstore.dto.request.AddToCartRequest;
import com.reins.bookstore.dto.response.CartItemDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * 将 Cart Entity 转换为 CartItemDTO（包含书籍详情）
     */
    private CartItemDTO toDTO(Cart cart) {
        Book book = bookRepository.findById(cart.getBookId()).orElse(null);
        return new CartItemDTO(
                cart.getId(),
                cart.getBookId(),
                cart.getUserId(),
                cart.getNumber(),
                book != null ? book.getTitle() : null,
                book != null ? book.getCover() : null,
                book != null && book.getPrice() != null ? book.getPrice().intValue() : 0
        );
    }

    /**
     * 查询用户的购物车
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        List<CartItemDTO> dtos = cartRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("items", dtos));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        Long userId = request.getUserId();
        Long bookId = request.getBookId();
        Integer quantity = request.getQuantity();

        // 检查购物车中是否已有此商品
        Cart existingItem = cartRepository.findByUserId(userId).stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // 更新数量
            existingItem.setNumber(existingItem.getNumber() + quantity);
            cartRepository.save(existingItem);
        } else {
            // 创建新的购物车项
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setBookId(bookId);
            cartItem.setNumber(quantity);
            cartRepository.save(cartItem);
        }

        return ResponseEntity.ok(Map.of("message", "Item added to cart"));
    }

    /**
     * 更新购物车项的数量
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
     * 清空用户的购物车
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        cartRepository.deleteByUserId(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}
