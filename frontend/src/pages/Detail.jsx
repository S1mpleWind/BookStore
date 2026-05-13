import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Button, Spin, Empty, InputNumber, App } from 'antd';
import { getBookById, addToCart } from '../api';
import { useUser } from '../components/UserContext';

/**
 * Detail 页面：根据 URL 参数 id 从后端加载并显示书籍详细信息。
 */
const Detail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useUser();
  const { message } = App.useApp();
  //? 需要用这个才能调用message
  const [quantity, setQuantity] = useState(1);
  const [bookState, setBookState] = useState({ requestId: null, book: null, error: '' });

  useEffect(() => {
    getBookById(id)
      .then((data) => {
        setBookState({ requestId: id, book: data, error: '' });
      })
      .catch((err) => {
        console.error('Failed to load book detail:', err);
        setBookState({ requestId: id, book: null, error: '未能加载书籍详情，请稍后重试。' });
      });
  }, [id]);

  const { requestId, book, error } = bookState;
  const loading = requestId !== id;

  const handleAddToCart = async () => {
    if (!user?.userId) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }

    try {
      console.log('[handleAddToCart] 开始添加到购物车', { userId: user.userId, bookId: book.id, quantity });
      await addToCart(user.userId, book.id, quantity);
      console.log('[handleAddToCart] 成功');

      message.success(`已添加 ${quantity} 本《${book.title}》到购物车`);
      setQuantity(1);
    } catch (err) {
      console.error('[handleAddToCart] 失败:', err);
      message.error('添加到购物车失败');
    }
  };

  const handleBuyNow = async () => {
    if (!user?.userId) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }

    try {
      // 先添加到购物车，再跳转到购物车页面
      await addToCart(user.userId, book.id, quantity);
      message.success(`已添加到购物车，请前往结算`);
      navigate('/cart');
    } catch (err) {
      message.error('操作失败');
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px' }}>
        <Spin size="large" tip="加载书籍详情中..." />
      </div>
    );
  }

  if (error || !book) {
    return (
      <div className="content-inner">
        <Link to="/" className="back">← 返回列表</Link>
        <Empty description={error || '未找到该书籍，请稍后重试。'} />
      </div>
    );
  }

  const priceText = (Number(book.price || 0) / 100).toFixed(2);
  const description = book.desc || book.description || '这本书还没有详细描述。';

  return (
    <div className="content-inner">
      <Link to="/" className="back">← 返回列表</Link>
      <section className="card book-detail">
        <div className="detail-grid">
          <div className="cover">
            <img src={book.cover} alt={book.title} />
          </div>
          <div className="info">
            <div className="detail-head">
              <p className="hero-kicker">BOOK DETAIL</p>
              <h2>{book.title}</h2>
              <p className="muted">作者：{book.author}</p>
              <p className="price-large">¥{priceText}</p>
              <div style={{ marginTop: '20px', marginBottom: '20px' }}>
                <span>数量: </span>
                <InputNumber 
                  min={1} 
                  max={999} 
                  value={quantity} 
                  onChange={setQuantity}
                  style={{ width: '100px' }}
                />
              </div>
            </div>

            <div className="detail-copy">
              <p className="detail-desc">{description}</p>
            </div>
          </div>
        </div>
      </section>

      {/* 独立操作区：加入购物车（次要）、立即购买（主色）、查看购物车（次要） */}
      <div className="detail-actions separate">
        <div className="action-left">
          <Button type="default" className="btn-left" onClick={handleAddToCart} aria-label="加入购物车">加入购物车</Button>
        </div>
        <div className="action-right">
          <Button type="primary" className="btn-right" onClick={handleBuyNow} aria-label="立即购买">立即购买</Button>
        </div>
      </div>
    </div>
  );
};

export default Detail;

