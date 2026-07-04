import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Input, Button, Spin, Empty } from 'antd';
import { getBooks } from '../api';

const recommendIdList = [1, 2];

const Home = () => {
  const [q, setQ] = useState('');
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadBooks = (title) => {
    setLoading(true);
    getBooks(title)
      .then(setBooks)
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadBooks(); }, []);

  const handleSearch = (value) => {
    if (value.trim()) {
      loadBooks(value.trim());
    } else {
      loadBooks();
    }
  };

  const recommended = useMemo(
    () => books.filter((b) => recommendIdList.includes(Number(b.id))),
    [books]
  );

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px' }}><Spin size="large" /></div>;
  }

  return (
    <div className="content-inner">
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

      <section className="catalog-shell">
        {books.length === 0 ? (
          <Empty description="暂无书籍数据" />
        ) : (
          <>
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