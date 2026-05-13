import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Button, Spin, Empty } from 'antd';
import { getBookById } from '../api';

/**
 * Detail 页面：根据 URL 参数 id 从后端加载并显示书籍详细信息。
 */
const Detail = () => {
  const { id } = useParams();
  const [notice, setNotice] = useState('');
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

  //TODO:
  const handleNoop = () => {
    setNotice('该按钮暂不接入后端处理。');
  };

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
          <Button type="default" className="btn-left" onClick={handleNoop} aria-label="加入购物车">加入购物车</Button>
        </div>
        <div className="action-right">
          <Button type="primary" className="btn-right" onClick={handleNoop} aria-label="立即购买">立即购买</Button>
        </div>
      </div>

      {notice ? <p className="inline-notice">{notice}</p> : null}
    </div>
  );
};

export default Detail;

