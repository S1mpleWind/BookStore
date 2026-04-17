import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

/**
 * Order 页面：显示刚才下单成功的书籍信息。
 * 为了演示，我们将模拟数据存储在本地。
 */
const Order = () => {
    const [orderInfo, setOrderInfo] = useState(null);

    // 在组件加载时，从 localStorage 获取订单信息并解析
    // 在里面调用useState，防止直接使用useState时，localStorage数据不正确导致页面崩溃
    useEffect(() => {
        try {
            const lastOrder = localStorage.getItem('lastOrder');
            if (!lastOrder) return;

            const parsed = JSON.parse(lastOrder);
            if (!parsed || !Array.isArray(parsed.items)) return;

            //重新渲染订单信息
            setOrderInfo({
                id: parsed.id || 'N/A',
                items: parsed.items,
                totalCount: Number(parsed.totalCount) || 0,
                totalPrice: Number(parsed.totalPrice) || 0,
            });
        } catch (error) {
            setOrderInfo(null);
        }
    }, []);

    if (!orderInfo) {
        return (
            <div className="content-inner">
                <section className="hero-panel">
                    <h1 className="page-title">暂无订单信息</h1>
                    <p className="muted">你还没有进行过结算，快去挑选心仪的书籍吧。</p>
                    <Link to="/" className="btn">去首页逛逛</Link>
                </section>
            </div>
        );
    }

    return (
        <div className="content-inner">
            <section className="hero-panel">
                <p className="hero-kicker">SUCCESSFUL ORDER</p>
                <h1 className="page-title">下单成功</h1>
                <p className="hero-desc">您的订单已确认，电子教材将直接发送至您的账户。</p>
            </section>

            <section className="order-details-card card">
                <div className="order-head">
                    <h2>订单详情</h2>
                    <span className="order-id-tag">单号: ORD-{orderInfo.id}</span>
                </div>
                <div className="summary-divider" />
                <div className="order-items-list">
                    {orderInfo.items.map((item) => (
                        <div key={item.id} className="order-item-row">
                            <span className="item-title">{item.title}</span>
                            <span className="item-qty">x{item.quantity}</span>
                            <span className="item-price">¥{(item.price * item.quantity).toFixed(2)}</span>
                        </div>
                    ))}
                </div>
                <div className="summary-divider" />
                <div className="order-footer">
                    <div className="summary-row">
                        <span>商品总数</span>
                        <strong>{orderInfo.totalCount} 本</strong>
                    </div>
                    <div className="summary-row summary-row-main">
                        <span>支付总额</span>
                        <strong>¥{orderInfo.totalPrice.toFixed(2)}</strong>
                    </div>
                </div>
            </section>

            <div className="detail-actions" style={{ marginTop: '24px' }}>
                <Link to="/" className="btn btn-outline">返回首页继续购买</Link>
            </div>
        </div>
    );
};

export default Order;