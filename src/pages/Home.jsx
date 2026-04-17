import React from 'react';
import { Link } from 'react-router-dom';
import booksData from '../data/books.json';

/**
 * Home 页面：显示所有书籍列表。
 * 数据重用自 hw1 的 JSON 格式。
 */
const Home = () => {
  const recommended = booksData.filter((b) => b.recommended);
  const others = booksData.filter((b) => !b.recommended);

  return (
    <div className="content-inner">
      <section className="hero-panel">
        <p className="hero-kicker">CURATED BOOKSHELF</p>
        <h1 className="page-title">精选经典教材</h1>
        <p className="hero-desc">从操作系统到计算机系统结构，聚焦计算机科学基础能力建设。</p>
      </section>

      <section className="catalog-shell" aria-label="推荐与书籍列表区域">
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
                  <p className="price">¥{book.price.toFixed(2)}</p>
                  <div className="card-actions">
                    <Link to={`/book/${book.id}`} className="btn">查看详情</Link>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}

        <div className="section-head" style={{ marginTop: 16 }}>
          <h2>全部书籍</h2>
          <p className="muted">点击查看详情</p>
        </div>

        <div className="book-list" aria-label="书籍列表">
          {(others.length === 0 ? booksData : others).map((book) => (
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
                <Link to={`/book/${book.id}`} className="btn">查看详情</Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
};

export default Home;

