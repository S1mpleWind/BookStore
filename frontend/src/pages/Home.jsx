import React, { useState, useMemo, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Input, Button, Spin, Empty } from 'antd';
import { getBooks } from '../api';

const recommendIdList = [1, 2]; //TODO: manually load recommend

/**
 * Home 页面：显示所有书籍列表。
 * 数据现已改为从后端 API 加载。
 */
const Home = () => {
  const [q, setQ] = useState('');
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getBooks()
      .then((data) => {
        setBooks(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to load books:', err);
        setLoading(false);
      });
  }, []);

  const recommended = useMemo(
    () => books.filter((b) => recommendIdList.includes(Number(b.id))),
    [books]
  );

  // 寻找搜索的书籍，对全局变量q进行查找
  // 使用 useMemo 来缓存过滤结果，避免每次渲染都进行过滤计算,可以复用
  const filtered = useMemo(() => {
    if (!q) return books;
    const key = q.trim().toLowerCase();
    return books.filter((b) => (b.title + ' ' + (b.author || '')).toLowerCase().includes(key));
  }, [q, books]);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px' }}>
        <Spin size="large" tip="加载书籍列表中..." />
      </div>
    );
  }

  return (
    <div className="content-inner">
      <section className="hero-panel">
        <p className="hero-kicker">CURATED BOOKSHELF</p>
        <h1 className="page-title">精选书籍</h1>

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
        {books.length === 0 ? (
          <Empty description="暂无书籍数据" />
        ) : (
          <>
            <div className="section-head">
              <h2>推荐书单</h2>
              <p className="muted">编辑推荐 · 精选优质读物</p>
            </div>

            {recommended && recommended.length > 0 && (
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
            )}

            <div className="section-head" style={{ marginTop: 16 }}>
              <h2>全部书籍</h2>
              <p className="muted">共 {filtered.length} 本 · 点击查看详情</p>
            </div>

            <div className="book-list" aria-label="书籍列表">
              {filtered.map((book) => (
                <article className="card" key={book.id}>
                  <figure className="book-cover-wrap">
                    <img src={book.cover} alt={book.title} />
                  </figure>
                  <div className="card-body">
                    <h3>{book.title}</h3>
                    <p className="muted">{book.author}</p>
                    <p className="price">¥{(book.price / 100).toFixed(2)}</p>
                  </div>
                  <div className="card-actions">
                    <Link to={`/book/${book.id}`}>
                      <Button type="primary">查看详情</Button>
                    </Link>
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

