import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Button } from 'antd';
import booksData from '../data/books.json';

/**
 * Detail 页面：根据 URL 参数 id 查找并显示书籍详细信息。
 * 集成了添加购物车的功能逻辑。
 */
const Detail = () => {
  const { id } = useParams();
  const [notice, setNotice] = useState('');
  // 从加载的 JSON 中查找指定 ID 的书籍
  const book = booksData.find((b) => String(b.id) === String(id));

  // 如果没有找到书籍（例如手动输入一个不存在的 ID）
  if (!book) {
    return (
      <div className="content-inner">
        <Link to="/" className="back">← 返回列表</Link>
        <p className="muted">未找到该书籍，请稍后重试。</p>
      </div>
    );
  }

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
              <p className="price-large">¥{book.price.toFixed(2)}</p>
            </div>

            <div className="detail-copy">
              <p className="detail-desc">{book.desc || '这本书还没有详细描述。'}</p>
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

