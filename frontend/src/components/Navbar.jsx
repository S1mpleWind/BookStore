import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useUser } from './UserContext';
import { Button } from 'antd';

/**
 * 主导航栏组件
 *
 * 根据用户角色（顾客/管理员）动态展示不同的导航菜单：
 * - 顾客：书籍浏览 / 个人信息 / 购物车 / 订单 / 我的统计
 * - 管理员：书籍浏览 / 书籍管理 / 用户管理 / 订单管理 / 数据统计
 *
 * 角色通过 UserContext 中的 user.identity 判断：
 * 0 = 顾客，1 = 管理员
 */
const Navbar = () => {
  const { logout, user } = useUser();
  const navigate = useNavigate();
  const isAdmin = user?.identity === 1;

  /** 退出登录：清除本地状态并跳转到登录页 */
  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <nav className="site-nav" aria-label="主导航">
      {/* 品牌标识 + 角色标签 */}
      <div className="brand-wrap">
        <div className="brand">电子书店</div>
        <div className="brand-role">{isAdmin ? '管理员' : '顾客'}</div>
      </div>

      {/* 导航链接列表 */}
      <ul>
        <li>
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            <span className="nav-icon">📚</span>
            <span className="nav-label">书籍浏览</span>
          </NavLink>
        </li>

        {/* ── 顾客菜单 ── */}
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
          /* ── 管理员菜单 ── */
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

      {/* 底部退出按钮 */}
      <div className="nav-footer">
        <Button className="nav-logout" onClick={handleLogout}>退出登录</Button>
      </div>
    </nav>
  );
};

export default Navbar;