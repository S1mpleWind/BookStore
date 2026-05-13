import React, { createContext, useContext, useState, useEffect } from 'react';

const UserContext = createContext();

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

  useEffect(() => {
    try {
      if (user) localStorage.setItem('user', JSON.stringify(user));
      else localStorage.removeItem('user');
    } catch (e) {
      // ignore
    }
  }, [user]);

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

  const logout = () => {
    setUser(null);
    try {
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('user');
    } catch (e) {}
  };

  const update = (patch) => setUser((u) => ({ ...(u || {}), ...(patch || {}) }));

  const isAuthenticated = Boolean(user);

  return (
    <UserContext.Provider value={{ user, login, logout, update, isAuthenticated }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => useContext(UserContext);

export default UserContext;
