import React from 'react';
import { NavLink } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { useCart } from './CartContext';

/**
 * Navbar 组件：侧边栏导航栏。
 * 使用 React Router 的 NavLink 以自动处理路由选中状态。
 */
const Navbar = () => {
  const { cartItems } = useCart();
  const totalCount = cartItems.reduce((acc, item) => acc + (item.quantity || 0), 0);
  const navigate = useNavigate();

  const handleLogout = () => {
    try {
      localStorage.removeItem('isLoggedIn');
    } catch (error) {
      // 忽略 localStorage 异常，继续导航
    }
    navigate('/login', { replace: true });
  };

  return (
    <nav className="site-nav" aria-label="主导航">
      <div className="brand-wrap">
        <div className="brand">电子书店</div>
        <p className="brand-subtitle">Computer Science Classics</p>
      </div>
      <ul>
        <li>
          <NavLink to="/" className={({ isActive }) => (isActive ? 'active' : '')}>
            书籍
          </NavLink>
        </li>
        <li>
          <NavLink to="/cart" className={({ isActive }) => (isActive ? 'active' : '')}>
            购物车
            <span className="cart-badge" aria-label="购物车数量">{totalCount}</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/order" className={({ isActive }) => (isActive ? 'active' : '')}>
            订单
          </NavLink>
        </li>
      </ul>

      <div className="nav-footer">
        <small>Spring 2026 UI Refresh</small>
        <button className="btn btn-outline nav-logout" onClick={handleLogout}>
          退出登录
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
