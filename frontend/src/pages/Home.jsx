import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Input, Button, Spin, Empty } from 'antd';
import { getBooks } from '../api';

/** 推荐书籍的 ID 列表（在首页突出展示） */
const recommendIdList = [1, 2];

/**
 * 首页——书籍浏览
 *
 * 功能：
 * - 展示所有书籍的卡片列表（推荐书单 + 全部书籍）
 * - 支持按书名搜索（调用后端 API 模糊匹配）
 * - 点击"查看详情"进入书籍详情页
 *
 * 数据流：
 * 页面加载 → useEffect → getBooks() → GET /api/v1/book → 后端查数据库 → 返回 JSON
 */
const Home = () => {
  const [q, setQ] = useState('');         // 搜索框输入值
  const [books, setBooks] = useState([]);  // 书籍列表数据
  const [loading, setLoading] = useState(true);

  /** 从后端加载书籍数据（支持按书名筛选） */
  const loadBooks = (title) => {
    setLoading(true);
    getBooks(title)
      .then(setBooks)
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  /** 页面加载时获取全部书籍 */
  useEffect(() => { loadBooks(); }, []);

  /** 搜索回调：输入不为空则按书名过滤，否则返回全部 */
  const handleSearch = (value) => {
    if (value.trim()) {
      loadBooks(value.trim());
    } else {
      loadBooks();
    }
  };

  /** 从全部书籍中筛选出推荐书籍（用于顶部的推荐区域） */
  const recommended = useMemo(
    () => books.filter((b) => recommendIdList.includes(Number(b.id))),
    [books]
  );

  // 加载中状态
  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px' }}><Spin size="large" /></div>;
  }

  return (
    <div className="content-inner">
      {/* ── 搜索区域 ── */}
      <section className="hero-panel">
        <p className="hero-kicker">CURATED BOOKSHELF</p>
        <h1 className="page-title">精选书籍</h1>
        <div style={{ margin: '14px auto 0', maxWidth: 560 }}>
          <Input.Search
            placeholder="搜索书名"
            allowClear
            enterButton
            value={q}
            onChange={(e) => setQ(e.target.value)}
            onSearch={handleSearch}
          />
        </div>
      </section>

      {/* ── 书籍列表 ── */}
      <section className="catalog-shell">
        {books.length === 0 ? (
          <Empty description="暂无书籍数据" />
        ) : (
          <>
            {/* 推荐书单区域 */}
            {recommended.length > 0 && (
              <>
                <div className="section-head">
                  <h2>推荐书单</h2>
                  <p className="muted">编辑推荐 · 精选优质读物</p>
                </div>
                <div className="featured-list" aria-label="推荐书籍">
                  {recommended.map((book) => (
                    <article className="card" key={book.id}>
                      <figure className="book-cover-wrap">
                        <img src={book.cover} alt={book.title} />
                      </figure>
                      <div className="featured-meta">
                        <span className="featured-badge">推荐</span>
                        <h3>{book.title}</h3>
                        <p className="muted">{book.author}</p>
                        {book.isbn && <p className="muted">ISBN：{book.isbn}</p>}
                        <p className="muted">库存：{book.inventory ?? '-'} 本</p>
                        <p className="price">¥{(book.price / 100).toFixed(2)}</p>
                        <div className="card-actions">
                          <Link to={`/book/${book.id}`}>
                            <Button type="primary">查看详情</Button>
                          </Link>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              </>
            )}

            {/* 全部书籍列表 */}
            <div className="section-head" style={{ marginTop: 16 }}>
              <h2>全部书籍</h2>
              <p className="muted">共 {books.length} 本</p>
            </div>
            <div className="book-list" aria-label="书籍列表">
              {books.map((book) => (
                <article className="card" key={book.id}>
                  <figure className="book-cover-wrap">
                    <img src={book.cover} alt={book.title} />
                  </figure>
                  <div className="card-body">
                    <h3>{book.title}</h3>
                    <p className="muted">{book.author}</p>
                    {book.isbn && <p className="muted">ISBN：{book.isbn}</p>}
                    <p className="muted">库存：{book.inventory ?? '-'} 本</p>
                    <p className="price">¥{(book.price / 100).toFixed(2)}</p>
                  </div>
                  <div className="card-actions">
                    <Link to={`/book/${book.id}`}><Button type="primary">查看详情</Button></Link>
                  </div>
                </article>
              ))}
            </div>
          </>
        )}
      </section>
    </div>
  );
};

export default Home;
