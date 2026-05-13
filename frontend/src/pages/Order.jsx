import React, { useEffect, useState, useCallback } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Button, Spin, Empty, Alert, App } from 'antd';
import { useUser } from '../components/UserContext';
import { getOrders } from '../api';

/**
 * Order 页面：显示用户的订单列表。
 */
const Order = () => {
    const { user } = useUser();
    const location = useLocation();
    const { message } = App.useApp();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [debugLines, setDebugLines] = useState([]);
    const flashMessage = location.state?.flashMessage || '';

    const debug = false //used for debugging

    const pushDebug = (line) => {
        if (debug)
        {  
            setDebugLines((current) => [...current, `[${new Date().toLocaleTimeString()}] ${line}`]);
            console.log('[Order debug]', line);
        }
    };

    // 在组件加载或用户登录时获取订单列表
    const fetchOrders = useCallback(async () => {
        setLoading(true);
        try {
            const result = await getOrders(user.userId);
            pushDebug(`getOrders 返回，orders=${Array.isArray(result.orders) ? result.orders.length : 'invalid'}`);
            if (result.orders && Array.isArray(result.orders)) {
                if (result.orders.length > 0) {
                    setOrders(result.orders);
                    pushDebug(`已显示后端订单 ${result.orders.length} 条`);
                    return;
                }
            }

            const lastOrderRaw = localStorage.getItem('lastOrder');
            if (lastOrderRaw) {
                const parsed = JSON.parse(lastOrderRaw);
                if (parsed && parsed.id) {
                    pushDebug('后端无订单，使用 localStorage lastOrder 兜底');
                    setOrders([{
                        id: parsed.id,
                        receiver: user.nickname || user.username || '未填写',
                        address: '未填写',
                        tel: '未填写',
                        createdAt: new Date().toISOString(),
                        items: parsed.items || [],
                        totalCount: parsed.totalCount || 0,
                        totalPrice: parsed.totalPrice || 0,
                    }]);
                }
            }
        } catch (error) {
            console.error('Failed to fetch orders:', error);
            pushDebug(`getOrders 失败：${error?.message || error}`);
            const lastOrderRaw = localStorage.getItem('lastOrder');
            if (lastOrderRaw) {
                try {
                    const parsed = JSON.parse(lastOrderRaw);
                    pushDebug('订单接口失败，继续使用 localStorage lastOrder 兜底');
                    setOrders([{
                        id: parsed.id || Date.now(),
                        receiver: user.nickname || user.username || '未填写',
                        address: '未填写',
                        tel: '未填写',
                        createdAt: new Date().toISOString(),
                        items: parsed.items || [],
                        totalCount: parsed.totalCount || 0,
                        totalPrice: parsed.totalPrice || 0,
                    }]);
                } catch {
                    message.error('无法加载订单');
                }
            } else {
                message.error('无法加载订单');
            }
        } finally {
            setLoading(false);
        }
    }, [user?.userId, user?.nickname, user?.username]);

    useEffect(() => {
        if (user?.userId) {
            pushDebug(`进入订单页，userId=${user.userId}, flashMessage=${flashMessage || 'none'}`);
            fetchOrders();
        }
    }, [user?.userId, flashMessage, fetchOrders]);

    if (loading) {
        return (
            <div className="content-inner">
                <Spin />
            </div>
        );
    }

    if (!user?.userId) {
        return (
            <div className="content-inner">
                <section className="hero-panel">
                    <h1 className="page-title">请先登录</h1>
                    <p className="muted">你需要登录才能查看订单。</p>
                    <Link to="/login">
                        <Button type="primary">去登录</Button>
                    </Link>
                </section>
            </div>
        );
    }

    if (!orders || orders.length === 0) {
        return (
            <div className="content-inner">
                <h1 className="page-title">我的订单</h1>
                {flashMessage ? (
                    <Alert
                        style={{ marginBottom: 16 }}
                        type="success"
                        showIcon
                        message={flashMessage}
                    />
                ) : null}
                <div className="empty-state">
                    <Empty description="暂无订单" />
                    <Link to="/">
                        <Button type="primary">去购物</Button>
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="content-inner">
            <h1 className="page-title">我的订单</h1>

            {flashMessage ? (
                <Alert
                    style={{ marginBottom: 16 }}
                    type="success"
                    showIcon
                    message={flashMessage}
                />
            ) : null}

            {debugLines.length > 0 ? (
                <div style={{ marginBottom: 16, padding: 12, background: '#f6f8fa', border: '1px solid #d0d7de', borderRadius: 8, fontSize: 12, lineHeight: 1.6 }}>
                    <div style={{ fontWeight: 600, marginBottom: 8 }}>订单调试信息</div>
                    {debugLines.map((line) => (
                        <div key={line}>{line}</div>
                    ))}
                </div>
            ) : null}

            <div className="orders-list">
                {orders.map((order) => (
                    <section className="order-details-card card" key={order.id}>
                        <div className="order-head">
                            <h2>订单详情</h2>
                            <span className="order-id-tag">单号: ORD-{order.id}</span>
                            <span className="order-date">下单时间: {new Date(order.createdAt).toLocaleDateString()}</span>
                        </div>
                        <div className="summary-divider" />
                        <div className="order-info">
                            <p>收货人：{order.receiver}</p>
                            <p>收货地址：{order.address}</p>
                            <p>联系电话：{order.tel}</p>
                        </div>
                        {Array.isArray(order.items) && order.items.length > 0 ? (
                            <>
                                <div className="summary-divider" />
                                <div className="order-items-list">
                                    {order.items.map((item) => (
                                        <div key={item.id} className="order-item-row">
                                            <span className="item-title">{item.title}</span>
                                            <span className="item-qty">x{item.number ?? item.quantity ?? 0}</span>
                                            <span className="item-price">¥{(((item.unitPrice ?? item.price ?? 0) / 100) * (item.number ?? item.quantity ?? 0)).toFixed(2)}</span>
                                        </div>
                                    ))}
                                </div>
                            </>
                        ) : null}
                        <div className="summary-divider" />
                        <div className="order-footer">
                            <div className="summary-row">
                                <span>商品总数</span>
                                <strong>{order.totalCount || 0} 本</strong>
                            </div>
                            <div className="summary-row summary-row-main">
                                <span>支付总额</span>
                                <strong>¥{Number(order.totalPrice || 0).toFixed(2)}</strong>
                            </div>
                        </div>
                    </section>
                ))}
            </div>

            <div className="detail-actions" style={{ marginTop: '24px' }}>
                <Link to="/">
                    <Button>继续购物</Button>
                </Link>
            </div>
        </div>
    );
};

export default Order;