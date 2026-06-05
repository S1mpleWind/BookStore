import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Button, Spin, Empty, InputNumber, App } from 'antd';
import { getBookById, addToCart } from '../api';
import { useUser } from '../components/UserContext';

const Detail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useUser();
  const { message } = App.useApp();
  const [quantity, setQuantity] = useState(1);
  const [bookState, setBookState] = useState({ requestId: null, book: null, error: '' });

  useEffect(() => {
    getBookById(id)
      .then((data) => setBookState({ requestId: id, book: data, error: '' }))
      .catch(() => setBookState({ requestId: id, book: null, error: '未能加载书籍详情' }));
  }, [id]);

  const { requestId, book, error } = bookState;
  const loading = requestId !== id;

  const handleAddToCart = async () => {
    if (!user?.userId) { message.warning('请先登录'); navigate('/login'); return; }
    try {
      await addToCart(user.userId, book.id, quantity);
      message.success(`已添加 ${quantity} 本《${book.title}》到购物车`);
      setQuantity(1);
    } catch { message.error('添加到购物车失败'); }
  };

  const handleBuyNow = async () => {
    if (!user?.userId) { message.warning('请先登录'); navigate('/login'); return; }
    try {
      await addToCart(user.userId, book.id, quantity);
      message.success('已添加到购物车');
      navigate('/cart');
    } catch { message.error('操作失败'); }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '100px' }}><Spin size="large" /></div>;
  if (error || !book) return <div className="content-inner"><Link to="/" className="back">← 返回列表</Link><Empty description={error || '未找到该书籍'} /></div>;

  const priceText = (Number(book.price || 0) / 100).toFixed(2);

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
              {book.publisher && <p className="muted">出版社：{book.publisher}</p>}
              {book.isbn && <p className="muted">ISBN：{book.isbn}</p>}
              <p className="price-large">¥{priceText}</p>
              <p className="muted">库存：{book.inventory ?? '-'} 本</p>
              <div style={{ marginTop: '20px', marginBottom: '20px' }}>
                <span>数量: </span>
                <InputNumber min={1} max={book.inventory || 1} value={quantity} onChange={setQuantity} style={{ width: '100px' }} />
              </div>
            </div>
            <div className="detail-copy">
              <p className="detail-desc">{book.desc || book.description || '暂无描述'}</p>
            </div>
          </div>
        </div>
      </section>
      <div className="detail-actions separate">
        <div className="action-left">
          <Button type="default" className="btn-left" onClick={handleAddToCart}>加入购物车</Button>
        </div>
        <div className="action-right">
          <Button type="primary" className="btn-right" onClick={handleBuyNow}>立即购买</Button>
        </div>
      </div>
    </div>
  );
};

export default Detail;