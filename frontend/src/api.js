const API_BASE = 'http://localhost:8080/api/v1';

async function postJson(url, payload) {
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  const contentType = response.headers.get('content-type') || '';
  let data = null;
  if (contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }
  if (!response.ok) {
    if (typeof data === 'object' && data.error) {
      throw new Error(JSON.stringify({ status: response.status, error: data.error, data }));
    }
    throw new Error(JSON.stringify({ status: response.status, data }));
  }
  return data;
}

async function putJson(url, payload) {
  const response = await fetch(url, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(JSON.stringify({ status: response.status, data }));
  return data;
}

async function del(url) {
  const response = await fetch(url, { method: 'DELETE', credentials: 'include' });
  const data = await response.json();
  if (!response.ok) throw new Error(JSON.stringify({ status: response.status, data }));
  return data;
}

async function getJson(url) {
  const response = await fetch(url, { credentials: 'include' });
  if (!response.ok) throw new Error(JSON.stringify({ status: response.status }));
  return await response.json();
}

// ---- Books ----
export async function getBooks(title) {
  let url = `${API_BASE}/book`;
  if (title) url += `?title=${encodeURIComponent(title)}`;
  return getJson(url);
}

export async function getBookById(id) {
  return getJson(`${API_BASE}/book/${id}`);
}

export async function addBook(bookData) {
  return postJson(`${API_BASE}/book`, bookData);
}

export async function updateBook(id, bookData) {
  return putJson(`${API_BASE}/book/${id}`, bookData);
}

export async function deleteBook(id) {
  return del(`${API_BASE}/book/${id}`);
}

// ---- Auth ----
export async function loginUser(credentials) {
  return postJson(`${API_BASE}/users/login`, credentials);
}

export async function registerUser(profile) {
  return postJson(`${API_BASE}/users/register`, profile);
}

export async function logoutUser() {
  return postJson(`${API_BASE}/users/logout`, {});
}

// ---- Users (admin) ----
export async function getUsersList() {
  return getJson(`${API_BASE}/users/list`);
}

export async function toggleUserStatus(userId) {
  return putJson(`${API_BASE}/users/${userId}/status`, {});
}

// ---- Cart ----
export async function getCart(userId) {
  return getJson(`${API_BASE}/cart/${userId}`);
}

export async function addToCart(userId, bookId, quantity = 1) {
  return postJson(`${API_BASE}/cart`, { userId, bookId, quantity });
}

export async function updateCartItem(cartItemId, quantity) {
  return putJson(`${API_BASE}/cart/${cartItemId}`, { quantity });
}

export async function deleteCartItem(cartItemId) {
  return del(`${API_BASE}/cart/${cartItemId}`);
}

export async function clearCart(userId) {
  return del(`${API_BASE}/cart/user/${userId}`);
}

// ---- Orders ----
export async function createOrder(orderData) {
  return postJson(`${API_BASE}/orders`, orderData);
}

export async function getMyOrders(params = {}) {
  const q = new URLSearchParams();
  if (params.start) q.append('start', params.start);
  if (params.end) q.append('end', params.end);
  if (params.bookTitle) q.append('bookTitle', params.bookTitle);
  const qs = q.toString();
  return getJson(`${API_BASE}/orders${qs ? '?' + qs : ''}`);
}

export async function getAllOrders(params = {}) {
  const q = new URLSearchParams();
  if (params.start) q.append('start', params.start);
  if (params.end) q.append('end', params.end);
  if (params.bookTitle) q.append('bookTitle', params.bookTitle);
  const qs = q.toString();
  return getJson(`${API_BASE}/orders/all${qs ? '?' + qs : ''}`);
}

export async function getOrderById(id) {
  return getJson(`${API_BASE}/order/${id}`);
}

// ---- Statistics ----
export async function getSalesRanking(start, end) {
  const q = new URLSearchParams();
  if (start) q.append('start', start);
  if (end) q.append('end', end);
  const qs = q.toString();
  return getJson(`${API_BASE}/statistics/sales${qs ? '?' + qs : ''}`);
}

export async function getConsumptionRanking(start, end) {
  const q = new URLSearchParams();
  if (start) q.append('start', start);
  if (end) q.append('end', end);
  const qs = q.toString();
  return getJson(`${API_BASE}/statistics/consumption${qs ? '?' + qs : ''}`);
}

export async function getMyStatistics(start, end) {
  const q = new URLSearchParams();
  if (start) q.append('start', start);
  if (end) q.append('end', end);
  const qs = q.toString();
  return getJson(`${API_BASE}/statistics/my-purchases${qs ? '?' + qs : ''}`);
}