import React, { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Input, Button } from 'antd';
import booksData from '../data/books.json';

/**
 * Home 页面：显示所有书籍列表。
 * 数据重用自 hw1 的 JSON 格式。
 */
const Home = () => {
  const [q, setQ] = useState('');

  const recommended = useMemo(() => booksData.filter((b) => b.recommended), []);

  // 寻找搜索的书籍，对全局变量q进行查找
  const filtered = useMemo(() => {
    if (!q) return booksData;
    const key = q.trim().toLowerCase();
    return booksData.filter((b) => (b.title + ' ' + b.author).toLowerCase().includes(key));
  }, [q]);

  return (
    <div className="content-inner">
      <section className="hero-panel">
        <p className="hero-kicker">CURATED BOOKSHELF</p>
        <h1 className="page-title">精选经典教材</h1>
        
        <div style={{ margin: '14px auto 0', maxWidth: 560 }}>
          <Input.Search
            placeholder="搜索书名或作者"
            allowClear
            enterButton
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
      </section>

      {/*推荐书单*/}
      <section className="catalog-shell" aria-label="推荐与书籍列表区域">
        <div className="section-head">
          <h2>推荐书单</h2>
          <p className="muted">编辑推荐 · 精选优质读物</p>
        </div>

        {recommended && recommended.length > 0 && (
          <div className="featured-list" aria-label="推荐书籍">
            {/* 使用map来循环列表 */}
            {recommended.map((book) => (
              <article className="card" key={book.id}>
                <figure className="book-cover-wrap">
                  <img src={book.cover} alt={book.title} />
                </figure>
                <div className="featured-meta">
                  <span className="featured-badge">推荐</span>
                  <h3>{book.title}</h3>
                  <p className="muted">{book.author}</p>
                  <p className="price">¥{book.price.toFixed(2)}</p>
                  <div className="card-actions">
                    <Link to={`/book/${book.id}`}>
                      <Button type="primary">查看详情</Button>
                    </Link>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}

        <div className="section-head" style={{ marginTop: 16 }}>
          <h2>全部书籍</h2>
          <p className="muted">共 {filtered.length} 本 · 点击查看详情</p>
        </div>

        <div className="book-list" aria-label="书籍列表">
          {/*  */}
          {filtered.map((book) => (
            <article className="card" key={book.id}>
              <figure className="book-cover-wrap">
                <img src={book.cover} alt={book.title} />
              </figure>
              <div className="card-body">
                <h3>{book.title}</h3>
                <p className="muted">{book.author}</p>
                <p className="price">¥{book.price.toFixed(2)}</p>
              </div>
              <div className="card-actions">
                <Link to={`/book/${book.id}`}>
                  <Button type="primary">查看详情</Button>
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
};

export default Home;

