import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/**
 * Login 页面：模拟登录逻辑。
 * 登录成功后写入 localStorage，并回跳到来源页（默认首页）。
 */
const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('demo');

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = (e) => {
    e.preventDefault();

    // 免验证登录：点击登录直接进入系统
    localStorage.setItem('isLoggedIn', 'true');
    navigate(from, { replace: true });
  };

  return (
    <div className="login-shell">
      <section className="login-panel" aria-label="登录面板">
        <p className="hero-kicker">WELCOME BACK</p>
        <h1 className="page-title">登录电子书店</h1>
        <p className="muted">默认用户名已填充，点击登录即可进入系统。</p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="username">用户名</label>
          <input
            id="username"
            name="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="请输入用户名"
          />

          <label htmlFor="password">密码</label>
          <input
            id="password"
            name="password"
            type="password"
            value=""
            readOnly
            placeholder="请输入密码"
          />

          <button className="btn login-submit" type="submit">
            登录
          </button>
        </form>
      </section>
    </div>
  );
};

export default Login;
