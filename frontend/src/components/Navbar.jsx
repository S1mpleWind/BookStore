import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useCart } from './CartContext';
import { useUser } from './UserContext';
import { Button } from 'antd';

const Navbar = () => {
  const { cartItems } = useCart();
  const { logout, user } = useUser();
  const navigate = useNavigate();
  const isAdmin = user?.identity === 1;
  const totalCount = cartItems.reduce((acc, item) => acc + (item.quantity || 0), 0);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <nav className="site-nav" aria-label="主导航">
      <div className="brand-wrap">
        <div className="brand">电子书店</div>
        <div className="brand-role">{isAdmin ? '管理员' : '顾客'}</div>
      </div>
      <ul>
        <li>
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            <span className="nav-icon">📚</span>
            <span className="nav-label">书籍浏览</span>
          </NavLink>
        </li>
        {!isAdmin ? (
          <>
            <li>
              <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">👤</span>
                <span className="nav-label">个人信息</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/cart" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">🧺</span>
                <span className="nav-label">购物车</span>
                {totalCount > 0 && <span className="cart-badge">{totalCount}</span>}
              </NavLink>
            </li>
            <li>
              <NavLink to="/order" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">📄</span>
                <span className="nav-label">订单</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/statistics" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">📊</span>
                <span className="nav-label">我的统计</span>
              </NavLink>
            </li>
          </>
        ) : (
          <>
            <li>
              <NavLink to="/manage-books" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">📝</span>
                <span className="nav-label">书籍管理</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/manage-users" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">👥</span>
                <span className="nav-label">用户管理</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin-orders" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">📋</span>
                <span className="nav-label">订单管理</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/statistics" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span className="nav-icon">📊</span>
                <span className="nav-label">数据统计</span>
              </NavLink>
            </li>
          </>
        )}
      </ul>

      <div className="nav-footer">
        <Button className="nav-logout" onClick={handleLogout}>退出登录</Button>
      </div>
    </nav>
  );
};

export default Navbar;