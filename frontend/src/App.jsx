import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { App as AntdApp, ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Detail from './pages/Detail';
import Cart from './pages/Cart';
import Order from './pages/Order';
import Login from './pages/Login';
import Profile from './pages/Profile';
import ManageBooks from './pages/ManageBooks';
import ManageUsers from './pages/ManageUsers';
import AdminOrders from './pages/AdminOrders';
import Statistics from './pages/Statistics';
import { CartProvider } from './components/CartContext';
import { UserProvider } from './components/UserContext';
import './styles/style.css';
import './styles/detail-actions.css';
import './styles/antd-overrides.css';

/**
 * 检查用户是否已登录
 * 从 localStorage 读取 isLoggedIn 标记
 * @returns {boolean}
 */
const isLoggedIn = () => {
  try { return localStorage.getItem('isLoggedIn') === 'true'; } catch { return false; }
};

/**
 * 检查当前用户是否为管理员
 * 从 localStorage 读取 user 对象，判断 identity 是否为 1
 * @returns {boolean}
 */
const isAdmin = () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.identity === 1;
  } catch { return false; }
};

/**
 * 路由守卫组件
 *
 * 功能：
 * 1. 未登录 → 重定向到 /login，并记录来源页面（登录后跳回）
 * 2. 已登录但权限不足（adminOnly） → 重定向到首页
 * 3. 通过 → 渲染子组件
 *
 * @param {{ children, adminOnly }} props
 *   children: 要保护的路由内容
 *   adminOnly: 是否需要管理员权限（默认 false）
 */
const ProtectedRoute = ({ children, adminOnly = false }) => {
  const location = useLocation();
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  if (adminOnly && !isAdmin()) {
    return <Navigate to="/" replace />;
  }
  return children;
};

/**
 * 应用路由配置
 *
 * 路由分三类：
 * 1. 公开路由 — /login（已登录则跳转首页）
 * 2. 登录用户通用 — /, /book/:id, /cart, /order, /profile, /statistics
 * 3. 管理员专用（adminOnly）— /manage-books, /manage-users, /admin-orders
 * 4. 兜底 — 未匹配路由重定向到首页或登录页
 */
const AppRoutes = ({ selectedBook, setSelectedBook }) => {
  const location = useLocation();
  const onLoginPage = location.pathname === '/login';

  return (
    <div className={`app-layout ${onLoginPage ? 'app-layout-login' : ''}`}>
      {/* 登录页不显示导航栏 */}
      {!onLoginPage ? <Navbar /> : null}
      <main className={`main-content ${onLoginPage ? 'main-content-login' : ''}`}>
        <Routes>
          {/* 登录页：已登录则跳首页 */}
          <Route path="/login" element={isLoggedIn() ? <Navigate to="/" replace /> : <Login />} />

          {/* 通用页面（需登录） */}
          <Route path="/" element={<ProtectedRoute><Home setSelectedBook={setSelectedBook} /></ProtectedRoute>} />
          <Route path="/book/:id" element={<ProtectedRoute><Detail selectedBook={selectedBook} /></ProtectedRoute>} />
          <Route path="/cart" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
          <Route path="/order" element={<ProtectedRoute><Order /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />

          {/* 顾客和管理员都可用 */}
          <Route path="/statistics" element={<ProtectedRoute><Statistics /></ProtectedRoute>} />

          {/* 管理员专用 */}
          <Route path="/manage-books" element={<ProtectedRoute adminOnly><ManageBooks /></ProtectedRoute>} />
          <Route path="/manage-users" element={<ProtectedRoute adminOnly><ManageUsers /></ProtectedRoute>} />
          <Route path="/admin-orders" element={<ProtectedRoute adminOnly><AdminOrders /></ProtectedRoute>} />

          {/* 未匹配路由 → 兜底重定向 */}
          <Route path="*" element={<Navigate to={isLoggedIn() ? '/' : '/login'} replace />} />
        </Routes>
      </main>
    </div>
  );
};

/**
 * 根组件
 *
 * 组件嵌套层级：
 * ConfigProvider（Ant Design 中文化）
 *   → AntdApp（Ant Design 全局配置）
 *     → UserProvider（用户全局状态）
 *       → CartProvider（购物车全局状态，现已迁移至后端）
 *         → BrowserRouter（React Router）
 *           → AppRoutes（路由配置）
 */
const App = () => {
  const [selectedBook, setSelectedBook] = useState(null);

  return (
    <ConfigProvider locale={zhCN}>
      <AntdApp>
        <UserProvider>
          <CartProvider>
            <BrowserRouter>
              <AppRoutes selectedBook={selectedBook} setSelectedBook={setSelectedBook} />
            </BrowserRouter>
          </CartProvider>
        </UserProvider>
      </AntdApp>
    </ConfigProvider>
  );
};

export default App;