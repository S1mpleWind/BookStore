import React, { createContext, useContext, useState, useEffect } from 'react';

/**
 * 用户全局状态 Context
 *
 * 提供用户登录状态、登录/登出/更新操作，跨页面共享。
 *
 * 数据持久化：
 * - localStorage 存储 user 对象和 isLoggedIn 标记
 * - 页面刷新后从 localStorage 恢复登录状态
 * - 退出登录时清除 localStorage
 */

const UserContext = createContext();

/** 从 localStorage 恢复用户数据 */
const loadUserFromStorage = () => {
  try {
    const raw = localStorage.getItem('user');
    if (!raw) return null;
    return JSON.parse(raw);
  } catch (e) {
    return null;
  }
};

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(loadUserFromStorage);

  /** 用户数据变化时同步到 localStorage */
  useEffect(() => {
    try {
      if (user) localStorage.setItem('user', JSON.stringify(user));
      else localStorage.removeItem('user');
    } catch (e) {
      // ignore
    }
  }, [user]);

  /** 登录：保存用户信息到 state 和 localStorage */
  const login = (payload = {}) => {
    const next = {
      userId: payload.userId ?? payload.id ?? null,
      username: payload.username || '',
      nickname: payload.nickname || payload.name || 'Guest',
      name: payload.nickname || payload.name || 'Guest',
      email: payload.email || '',
      identity: payload.identity ?? 0,
      lastLogin: Date.now(),
      ...payload,
    };

    setUser(next);
    try { localStorage.setItem('isLoggedIn', 'true'); } catch (e) {}
  };

  /** 登出：清除 state 和 localStorage */
  const logout = () => {
    setUser(null);
    try {
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('user');
    } catch (e) {}
  };

  /** 更新用户字段（部分更新） */
  const update = (patch) => setUser((u) => ({ ...(u || {}), ...(patch || {}) }));

  const isAuthenticated = Boolean(user);

  return (
    <UserContext.Provider value={{ user, login, logout, update, isAuthenticated }}>
      {children}
    </UserContext.Provider>
  );
};

/** 自定义 Hook，组件中通过 useUser() 获取用户状态 */
export const useUser = () => useContext(UserContext);

export default UserContext;
