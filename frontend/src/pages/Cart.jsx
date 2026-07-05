import React, { useState, useEffect, useCallback } from 'react';
import { useUser } from '../components/UserContext';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Empty, Spin, Modal, Input, App } from 'antd';
import { getCart, deleteCartItem, updateCartItem, getBookById, createOrder, clearCart } from '../api';

/**
 * 购物车页面
 * 显示当前用户的购物车内容，支持修改数量、删除商品、结算下单。
 *
 * 数据流：
 * 1. 页面加载 → getCart(userId) → 后端查 cart_item 表 → 返回购物车项
 * 2. 购物车项只包含 bookId，还需要 getBookById() 获取每本书的详细信息
 * 3. 结算时 → createOrder() → 后端扣库存、生成订单、清空购物车
 *
 * 注意：购物车数据存在后端数据库（cart_item 表）中，而非本地 localStorage。
 */
const Cart = () => {
    const { user } = useUser();
    const navigate = useNavigate();
    const { message } = App.useApp();
    const [cartItems, setCartItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [debugLines, setDebugLines] = useState([]);          // 调试日志
    const [showConfirmModal, setShowConfirmModal] = useState(false);  // 结算确认弹窗
    const [receiverInput, setReceiverInput] = useState('');    // 收货人
    const [addressInput, setAddressInput] = useState('东川路800号');  // 收货地址
    const [telInput, setTelInput] = useState('123456789');     // 联系电话

    // 调试模式开关（设为 true 可查看调试信息）
    const debug = false;

    /** 记录调试日志 */
    const pushDebug = (line) => {
        if (debug){
            setDebugLines((current) => [...current, `[${new Date().toLocaleTimeString()}] ${line}`]);
            console.log('[Cart debug]', line);
        }
    };

    /**
     * 从后端加载购物车数据
     * 1. 先调用 getCart() 获取购物车项（含 bookId）
     * 2. 再对每个 item 调用 getBookById() 获取书籍详情（封面、书名、价格）
     */
    const fetchCartData = useCallback(async () => {
        if (!user?.userId) return;

        pushDebug(`开始加载购物车，userId=${user.userId}`);
        setLoading(true);
        try {
            const result = await getCart(user.userId);
            pushDebug(`购物车接口返回，items=${Array.isArray(result.items) ? result.items.length : 'invalid'}`);
            if (result.items && Array.isArray(result.items)) {
                // 逐个获取书籍详情（Promise.all 并发请求）
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

    /** 删除购物车中的某一项 */
    const handleRemoveItem = async (cartItemId) => {
        try {
            await deleteCartItem(cartItemId);
            setCartItems(cartItems.filter((item) => item.id !== cartItemId));
            message.success('已从购物车移除');
        } catch {
            message.error('删除失败');
        }
    };

    /** 修改某件商品的数量 */
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

    /** 购物车汇总计算 */
    const totalCount = cartItems.reduce((acc, item) => acc + (item.number || 0), 0);
    const totalPrice = cartItems.reduce((acc, item) => acc + ((item.price || 0) * (item.number || 0)), 0) / 100;
    const bookKinds = cartItems.length;

    /** 打开结算确认弹窗 */
    const handleCheckout = async () => {
        if (!user?.userId) {
            message.warning('请先登录');
            navigate('/login');
            return;
        }

        // 设置默认收货信息
        setReceiverInput(user?.nickname || user?.username || '未填写');
        setAddressInput('东川路800号');
        setTelInput('123456789');
        setShowConfirmModal(true);
        pushDebug(`打开确认弹窗，默认 receiver=${user?.nickname || user?.username}, address=东川路800号, tel=123456789`);
    };

    /** 提交订单 */
    const submitOrderFromModal = async () => {
        setShowConfirmModal(false);
        pushDebug(`用户确认弹窗：receiver=${receiverInput}, address=${addressInput}, tel=${telInput}`);

        try {
            const orderPayload = {
                userId: user.userId,
                receiver: receiverInput,
                address: addressInput,
                tel: telInput,
            };

            pushDebug(`准备调用 createOrder，payload=${JSON.stringify(orderPayload)}`);

            // 调用后端创建订单（后端事务中会扣库存、生成订单项、清空购物车）
            await createOrder(orderPayload);
            pushDebug('createOrder 成功，准备跳转到 /order');

            navigate('/order', {
                state: {
                    flashMessage: '订单已生成，请查看',
                },
            });

            pushDebug('navigate(/order) 已执行');

            // 后端 createOrder 已清空购物车，前端再次调用 clearCart 作为双重保障
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
                /* ── 空购物车 ── */
                <div className="empty-state">
                    <Empty description="购物车目前是空的" />
                    <Link to="/">
                        <Button type="primary">返回去逛逛</Button>
                    </Link>
                </div>
            ) : (
                /* ── 购物车内容 ── */
                <div className="cart-layout">
                    {/* 商品列表 */}
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
                                        {/* 数量加减 */}
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

                    {/* 结算摘要 */}
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

                        {/* 调试信息面板 */}
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

            {/* ── 结算确认弹窗 ── */}
            <Modal
                open={showConfirmModal}
                title="确认订单"
                onOk={submitOrderFromModal}
                onCancel={() => setShowConfirmModal(false)}
                okText="确认并下单"
                cancelText="取消"
            >
                <div style={{ marginBottom: 12 }}>
                    <div style={{ marginBottom: 6, fontWeight: 600 }}>收货人</div>
                    <Input value={receiverInput} onChange={(e) => setReceiverInput(e.target.value)} />
                </div>
                <div style={{ marginBottom: 12 }}>
                    <div style={{ marginBottom: 6, fontWeight: 600 }}>收货地址</div>
                    <Input value={addressInput} onChange={(e) => setAddressInput(e.target.value)} />
                </div>
                <div>
                    <div style={{ marginBottom: 6, fontWeight: 600 }}>联系电话</div>
                    <Input value={telInput} onChange={(e) => setTelInput(e.target.value)} />
                </div>
            </Modal>
        </div>
    );
};

export default Cart;
