import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Detail from './pages/Detail';
import Cart from './pages/Cart';
import Order from './pages/Order';
import Login from './pages/Login';
import Profile from './pages/Profile';
import { CartProvider } from './components/CartContext';
import { UserProvider } from './components/UserContext';
import './styles/style.css';

const isLoggedIn = () => {
  try {
    return localStorage.getItem('isLoggedIn') === 'true';
  } catch {
    return false;
  }
};

const ProtectedRoute = ({ children }) => {
  const location = useLocation();

  if (!isLoggedIn()) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
};

const AppRoutes = ({ selectedBook, setSelectedBook }) => {
  const location = useLocation();
  const onLoginPage = location.pathname === '/login';

  return (
    <div className={`app-layout ${onLoginPage ? 'app-layout-login' : ''}`}>
      {/* 只有在非登录页才渲染侧边导航栏 */}
      {!onLoginPage ? <Navbar /> : null}
      <main className={`main-content ${onLoginPage ? 'main-content-login' : ''}`}>
        <Routes>
          <Route path="/login" element={isLoggedIn() ? <Navigate to="/" replace /> : <Login />} />

          <Route
            path="/"
            element={(
              <ProtectedRoute>
                <Home setSelectedBook={setSelectedBook} />
              </ProtectedRoute>
            )}
          />

          <Route
            path="/book/:id"
            element={(
              <ProtectedRoute>
                <Detail selectedBook={selectedBook} />
              </ProtectedRoute>
            )}
          />

          <Route path="/cart" element={(
            <ProtectedRoute>
              <Cart />
            </ProtectedRoute>
          )} />

          <Route path="/order" element={(
            <ProtectedRoute>
              <Order />
            </ProtectedRoute>
          )} />

          <Route path="/profile" element={(
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          )} />

          {/*如果不是登录，就定向到./*/}
          <Route path="*" element={<Navigate to={isLoggedIn() ? '/' : '/login'} replace />} />
        </Routes>
      </main>
    </div>
  );
};

/**
 * App 组件：以 hw1 为基础重构。
 * 引入 CartProvider 管理全局购物车状态。
 */
const App = () => {
  const [selectedBook, setSelectedBook] = useState(null);

  return (
    <UserProvider>
      <CartProvider>
        <BrowserRouter>
          <AppRoutes selectedBook={selectedBook} setSelectedBook={setSelectedBook} />
        </BrowserRouter>
      </CartProvider>
    </UserProvider>
  );
};

export default App;
