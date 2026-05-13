import React, { useState, useEffect, useCallback } from 'react';
import { useUser } from '../components/UserContext';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Empty, Spin, message } from 'antd';
import { getCart, deleteCartItem, updateCartItem, getBookById, createOrder, clearCart } from '../api';

/**
 * Cart 页面：显示购物车内容。
 * 支持从后端加载购物车数据，并在结算前弹出确认框。
 */
const Cart = () => {
    const { user } = useUser();
    const navigate = useNavigate();
    const [cartItems, setCartItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [debugLines, setDebugLines] = useState([]);

    const debug = false

    const pushDebug = (line) => {
        if (debug){
            setDebugLines((current) => [...current, `[${new Date().toLocaleTimeString()}] ${line}`]);
            console.log('[Cart debug]', line);
        }
    };

    const fetchCartData = useCallback(async () => {
        if (!user?.userId) return;

        pushDebug(`开始加载购物车，userId=${user.userId}`);
        setLoading(true);
        try {
            const result = await getCart(user.userId);
            pushDebug(`购物车接口返回，items=${Array.isArray(result.items) ? result.items.length : 'invalid'}`);
            if (result.items && Array.isArray(result.items)) {
                const itemsWithDetails = await Promise.all(
                    result.items.map(async (item) => {
                        try {
                            const bookData = await getBookById(item.bookId);
                            return {
                                ...item,
                                title: bookData.title,
                                cover: bookData.cover,
                                price: bookData.price,
                                author: bookData.author,
                            };
                        } catch (fetchError) {
                            console.error(`Failed to fetch book ${item.bookId}:`, fetchError);
                            return item;
                        }
                    })
                );
                setCartItems(itemsWithDetails);
                message.success('购物车已加载');
                pushDebug(`购物车商品详情加载完成，展示 ${itemsWithDetails.length} 条`);
            }
        } catch (error) {
            console.error('Failed to fetch cart:', error);
            message.error('无法加载购物车');
            pushDebug(`加载购物车失败：${error?.message || error}`);
        } finally {
            setLoading(false);
        }
    }, [user?.userId]);

    useEffect(() => {
        fetchCartData();
    }, [fetchCartData]);

    const handleRemoveItem = async (cartItemId) => {
        try {
            await deleteCartItem(cartItemId);
            setCartItems(cartItems.filter((item) => item.id !== cartItemId));
            message.success('已从购物车移除');
        } catch {
            message.error('删除失败');
        }
    };

    const handleUpdateQuantity = async (cartItemId, newQuantity) => {
        if (newQuantity <= 0) {
            handleRemoveItem(cartItemId);
            return;
        }
        try {
            await updateCartItem(cartItemId, newQuantity);
            setCartItems(cartItems.map((item) => (
                item.id === cartItemId ? { ...item, number: newQuantity } : item
            )));
            message.success('数量已更新');
        } catch {
            message.error('更新失败');
        }
    };

    const totalCount = cartItems.reduce((acc, item) => acc + (item.number || 0), 0);
    const totalPrice = cartItems.reduce((acc, item) => acc + ((item.price || 0) * (item.number || 0)), 0) / 100;
    const bookKinds = cartItems.length;

    // TODO bugs here
    const handleCheckout = async () => {
        if (!user?.userId) {
            message.warning('请先登录');
            navigate('/login');
            return;
        }

        pushDebug(`点击结算，cartItems=${cartItems.length}, totalCount=${totalCount}, totalPrice=${totalPrice.toFixed(2)}`);
        const confirmed = window.confirm(`当前订单共 ${totalCount} 件商品，合计 ¥${totalPrice.toFixed(2)}。确认后将创建订单并跳转到订单页。`);
        pushDebug(`确认弹窗结果：${confirmed ? '确认' : '取消'}`);

        if (!confirmed) return;

        try {
            const orderPayload = {
                userId: user.userId,
                receiver: user.nickname || user.username || '未填写',
                address: '未填写',
                tel: '未填写',
            };

            pushDebug(`准备调用 createOrder，payload=${JSON.stringify(orderPayload)}`);

            await createOrder(orderPayload);
            pushDebug('createOrder 成功，准备跳转到 /order');

            navigate('/order', {
                state: {
                    flashMessage: '订单已生成，请前往订单页查看',
                },
            });

            pushDebug('navigate(/order) 已执行');

            clearCart(user.userId).catch((error) => {
                console.error('Failed to clear cart after checkout:', error);
                pushDebug(`清空购物车失败：${error?.message || error}`);
            });
        } catch (error) {
            console.error('Failed to create order:', error);
            message.error('提交订单失败');
            let text;
            try {
                text = error instanceof Error ? error.message : JSON.stringify(error);
            } catch (e) {
                text = String(error);
            }
            pushDebug(`createOrder 失败：${text}`);
        }
    };

    if (loading) {
        return (
            <div className="content-inner">
                <Spin />
            </div>
        );
    }

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
                                        <p className="price">单价：¥{((item.price || 0) / 100).toFixed(2)}</p>
                                        <div className="qty-controls">
                                            <span>数量：</span>
                                            <Button size="small" onClick={() => handleUpdateQuantity(item.id, item.number - 1)}>-</Button>
                                            <span className="qty-value">{item.number}</span>
                                            <Button size="small" onClick={() => handleUpdateQuantity(item.id, item.number + 1)}>+</Button>
                                        </div>
                                        <div className="detail-actions">
                                            <Button danger onClick={() => handleRemoveItem(item.id)}>移除</Button>
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
                        {debugLines.length > 0 ? (
                            <div style={{ marginTop: 16, padding: 12, background: '#f6f8fa', border: '1px solid #d0d7de', borderRadius: 8, fontSize: 12, lineHeight: 1.6 }}>
                                <div style={{ fontWeight: 600, marginBottom: 8 }}>结算调试信息</div>
                                {debugLines.map((line) => (
                                    <div key={line}>{line}</div>
                                ))}
                            </div>
                        ) : null}
                    </aside>
                </div>
            )}
        </div>
    );
};

export default Cart;
