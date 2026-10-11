// 客户端保存访问凭证；用户身份和权限始终由后端验证。
const TOKEN_KEY = 'access_token';
export const AUTH_CHANGED = 'bookstore:auth-changed';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem('user');
  localStorage.removeItem('isLoggedIn');
  window.dispatchEvent(new Event(AUTH_CHANGED));
}

export function authorizationHeaders(headers = {}) {
  const token = getToken();
  return token ? { ...headers, Authorization: `Bearer ${token}` } : headers;
}
