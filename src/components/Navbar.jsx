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
        
      </div>
      <ul>
        <li>
          <NavLink to="/" className={({ isActive }) => (isActive ? 'active' : '')}>
            <span className="nav-icon">📚</span>
            <span className="nav-label">书籍</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/cart" className={({ isActive }) => (isActive ? 'active' : '')}>
            <span className="nav-icon">🧺</span>
            <span className="nav-label">购物车</span>
            <span className="cart-badge" aria-label="购物车数量" />
          </NavLink>
        </li>
        <li>
          <NavLink to="/order" className={({ isActive }) => (isActive ? 'active' : '')}>
            <span className="nav-icon">📄</span>
            <span className="nav-label">订单</span>
          </NavLink>
        </li>
      </ul>

      <div className="nav-footer">
        <button className="btn btn-outline nav-logout" onClick={handleLogout}>
          退出登录
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
