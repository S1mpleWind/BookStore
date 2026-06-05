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

const isLoggedIn = () => {
  try { return localStorage.getItem('isLoggedIn') === 'true'; } catch { return false; }
};

const isAdmin = () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.identity === 1;
  } catch { return false; }
};

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

const AppRoutes = ({ selectedBook, setSelectedBook }) => {
  const location = useLocation();
  const onLoginPage = location.pathname === '/login';

  return (
    <div className={`app-layout ${onLoginPage ? 'app-layout-login' : ''}`}>
      {!onLoginPage ? <Navbar /> : null}
      <main className={`main-content ${onLoginPage ? 'main-content-login' : ''}`}>
        <Routes>
          <Route path="/login" element={isLoggedIn() ? <Navigate to="/" replace /> : <Login />} />

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

          <Route path="*" element={<Navigate to={isLoggedIn() ? '/' : '/login'} replace />} />
        </Routes>
      </main>
    </div>
  );
};

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