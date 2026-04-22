import React from 'react';
import { useCart } from '../components/CartContext';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Empty, Space } from 'antd';

/**
 * Cart 页面：显示购物车内容。
 * 实现了从 hw1 静态迁移到 React 动态逻辑的重构。
 */
const Cart = () => {
    const { cartItems, removeFromCart, updateQuantity, clearCart } = useCart();
    const navigate = useNavigate();

    // 计算总价
    const totalCount = cartItems.reduce((acc, item) => acc + (item.quantity || 0), 0);
    const totalPrice = cartItems.reduce((acc, item) => acc + ((item.price || 0) * (item.quantity || 0)), 0);
    const bookKinds = cartItems.length;

    // 处理去结算逻辑
    const handleCheckout = () => {
        const orderInfo = {
            id: Math.floor(Math.random() * 1000000),
            items: cartItems,
            totalCount,
            totalPrice
        };
        // 保存订单数据到 localStorage
        localStorage.setItem('lastOrder', JSON.stringify(orderInfo));
        // 清空购物车数据
        clearCart();
        // 跳转到订单页面
        navigate('/order');
    };

    return (
        <div className="content-inner">
            <h1 className="page-title">我的购物车</h1>
            
            {cartItems.length === 0 ? (
                <div className="empty-state">
                    <Empty description="购物车目前是空的" />
                    <Link to="/">
                        <Button type="primary">返回去逛逛</Button>
                    </Link>
                </div>
            ) : (
                <div className="cart-layout">
                    <section className="cart-list" aria-label="购物车条目列表">
                        {cartItems.map((item) => (
                            <article className="card book-detail cart-item" key={item.id}>
                                <div className="detail-grid">
                                    <div className="cover">
                                        <img src={item.cover} alt={item.title} />
                                    </div>
                                    <div className="info">
                                        <h2>{item.title}</h2>
                                        <p className="muted">作者：{item.author}</p>
                                        <p className="price">单价：¥{(item.price || 0).toFixed(2)}</p>
                                        <div className="qty-controls">
                                            <span>数量：</span>
                                            <Button size="small" onClick={() => updateQuantity(item.id, -1)}>-</Button>
                                            <span className="qty-value">{item.quantity}</span>
                                            <Button size="small" onClick={() => updateQuantity(item.id, 1)}>+</Button>
                                        </div>
                                        <div className="detail-actions">
                                            <Button danger onClick={() => removeFromCart(item.id)}>移除</Button>
                                        </div>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </section>

                    <aside className="cart-summary card" aria-label="购物车汇总">
                        <h2>结算摘要</h2>
                        <p className="muted">当前已选择 <strong>{bookKinds}</strong> 种书籍</p>
                        <div className="summary-row">
                            <span>总件数</span>
                            <strong>{totalCount}</strong>
                        </div>
                        <div className="summary-row">
                            <span>配送方式</span>
                            <strong>电子交付</strong>
                        </div>
                        <div className="summary-divider" />
                        <div className="summary-row summary-row-main">
                            <span>总计</span>
                            <strong>¥{totalPrice.toFixed(2)}</strong>
                        </div>
                        <Button type="primary" onClick={handleCheckout}>确认下单并结算</Button>
                    </aside>
                </div>
            )}
        </div>
    );
};

export default Cart;
