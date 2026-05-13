package com.reins.bookstore.controller;

import com.reins.bookstore.entity.Cart;
import com.reins.bookstore.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    /**
     * 查询用户的购物车
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        return ResponseEntity.ok(Map.of("items", cartItems));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        Long bookId = Long.parseLong(params.get("bookId").toString());
        Integer quantity = Integer.parseInt(params.get("quantity").toString());

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
