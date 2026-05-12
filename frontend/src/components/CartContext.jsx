import React, { createContext, useContext, useState, useEffect } from 'react';

// 创建购物车 Context
const CartContext = createContext();

const normalizeCartItem = (item) => ({
  id: item?.id,
  title: item?.title || '未命名书籍',
  author: item?.author || '未知作者',
  cover: item?.cover || '',
  price: Number(item?.price) || 0,
  quantity: Math.max(1, Number(item?.quantity) || 1),
});


// 从 localStorage 加载购物车数据，确保数据格式正确
const loadCartFromStorage = () => {
  try {
    const savedCart = localStorage.getItem('cart');
    if (!savedCart) return [];

    const parsed = JSON.parse(savedCart);
    if (!Array.isArray(parsed)) return [];

    return parsed
      .filter((item) => item && item.id !== undefined && item.id !== null)
      .map(normalizeCartItem);
  } catch (error) {
    return [];
  }
};

/**
 * CartProvider 组件：提供购物车状态。
 * 在重构中，模拟了管理购物车商品（添加、删除、清空等状态）的功能。
 */
export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState(loadCartFromStorage);

  // 当购物车更新时同步到 localStorage
  useEffect(() => {
    try {
      localStorage.setItem('cart', JSON.stringify(cartItems));
    } catch (error) {
      // 忽略本地的
    }
  }, [cartItems]);

  // 添加到购物车功能
  const addToCart = (book) => {
    const nextBook = normalizeCartItem(book);

    setCartItems(prev => {
      const existing = prev.find(item => item.id === nextBook.id);
      if (existing) {
        return prev.map(item => 
          item.id === nextBook.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }

      return [...prev, { ...nextBook, quantity: 1 }];
    });
  };

  // 从购物车移除
  const removeFromCart = (bookId) => {
    setCartItems(prev => prev.filter(item => item.id !== bookId));
  };

  // 更新数量
  const updateQuantity = (bookId, delta) => {
    setCartItems(prev => prev.map(item => {
      if (item.id === bookId) {
        const nextQty = (item.quantity || 0) + delta;
        return nextQty > 0 ? { ...item, quantity: nextQty } : item;
      }
      return item;
    }));
  };

  // 清空购物车
  const clearCart = () => {
    setCartItems([]);
  };

  return (
    <CartContext.Provider value={{ cartItems, addToCart, removeFromCart, updateQuantity, clearCart }}>
      {children}
    </CartContext.Provider>
  );
};

// 自定义 Hook 以方便使用此 Context
export const useCart = () => useContext(CartContext);
