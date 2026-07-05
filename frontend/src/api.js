/**
 * API 服务层
 *
 * 封装所有后端 REST API 的调用，统一处理：
 * - HTTP 方法（GET/POST/PUT/DELETE）
 * - 请求体序列化（JSON）
 * - 响应体解析（JSON / text）
 * - 错误信息格式化
 * - 跨域凭据（credentials: 'include' 携带 Session Cookie）
 *
 * 使用方式：组件中 import { getBooks, loginUser, ... } from '../api'
 * 不再需要在组件中直接操作 fetch。
 */

/** 后端 API 基础地址（Spring Boot 默认端口 8080） */
const API_BASE = 'http://localhost:8080/api/v1';

/**
 * 通用 POST 请求封装
 * @param {string} url 请求地址
 * @param {object} payload 请求体对象（自动 JSON 序列化）
 * @returns {Promise<object|string>} 解析后的响应数据
 * @throws {Error} 封装了 HTTP 状态码和错误信息的 Error 对象
 */
async function postJson(url, payload) {
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',      // 携带 Session Cookie（JSESSIONID）
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
    // 将错误信息格式化为 Error，方便前端 catch 时统一处理
    if (typeof data === 'object' && data.error) {
      throw new Error(JSON.stringify({ status: response.status, error: data.error, data }));
    }
    throw new Error(JSON.stringify({ status: response.status, data }));
  }
  return data;
}

/**
 * 通用 PUT 请求封装
 * @param {string} url 请求地址
 * @param {object} payload 请求体对象
 * @returns {Promise<object>}
 */
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

/**
 * 通用 DELETE 请求封装
 * @param {string} url 请求地址
 * @returns {Promise<object>}
 */
async function del(url) {
  const response = await fetch(url, { method: 'DELETE', credentials: 'include' });
  const data = await response.json();
  if (!response.ok) throw new Error(JSON.stringify({ status: response.status, data }));
  return data;
}

/**
 * 通用 GET 请求封装
 * @param {string} url 请求地址
 * @returns {Promise<object>}
 */
async function getJson(url) {
  const response = await fetch(url, { credentials: 'include' });
  if (!response.ok) throw new Error(JSON.stringify({ status: response.status }));
  return await response.json();
}

// ═══════════════════════════════════════════
//  书籍相关 API
// ═══════════════════════════════════════════

/** 获取书籍列表（支持按书名搜索） */
export async function getBooks(title) {
  let url = `${API_BASE}/book`;
  if (title) url += `?title=${encodeURIComponent(title)}`;
  return getJson(url);
}

/** 获取单本书籍详情 */
export async function getBookById(id) {
  return getJson(`${API_BASE}/book/${id}`);
}

/** 管理员：添加新书 */
export async function addBook(bookData) {
  return postJson(`${API_BASE}/book`, bookData);
}

/** 管理员：修改书籍信息 */
export async function updateBook(id, bookData) {
  return putJson(`${API_BASE}/book/${id}`, bookData);
}

/** 管理员：删除书籍 */
export async function deleteBook(id) {
  return del(`${API_BASE}/book/${id}`);
}

// ═══════════════════════════════════════════
//  用户认证相关 API
// ═══════════════════════════════════════════

/** 用户登录 */
export async function loginUser(credentials) {
  return postJson(`${API_BASE}/users/login`, credentials);
}

/** 用户注册 */
export async function registerUser(profile) {
  return postJson(`${API_BASE}/users/register`, profile);
}

/** 用户登出 */
export async function logoutUser() {
  return postJson(`${API_BASE}/users/logout`, {});
}

// ═══════════════════════════════════════════
//  管理员：用户管理 API
// ═══════════════════════════════════════════

/** 管理员：获取所有用户列表 */
export async function getUsersList() {
  return getJson(`${API_BASE}/users/list`);
}

/** 管理员：禁用/解禁用户 */
export async function toggleUserStatus(userId) {
  return putJson(`${API_BASE}/users/${userId}/status`, {});
}

// ═══════════════════════════════════════════
//  购物车相关 API
// ═══════════════════════════════════════════

/** 获取用户的购物车内容 */
export async function getCart(userId) {
  return getJson(`${API_BASE}/cart/${userId}`);
}

/** 添加商品到购物车 */
export async function addToCart(userId, bookId, quantity = 1) {
  return postJson(`${API_BASE}/cart`, { userId, bookId, quantity });
}

/** 修改购物车项的数量 */
export async function updateCartItem(cartItemId, quantity) {
  return putJson(`${API_BASE}/cart/${cartItemId}`, { quantity });
}

/** 删除购物车中的某一项 */
export async function deleteCartItem(cartItemId) {
  return del(`${API_BASE}/cart/${cartItemId}`);
}

/** 清空用户的所有购物车项 */
export async function clearCart(userId) {
  return del(`${API_BASE}/cart/user/${userId}`);
}

// ═══════════════════════════════════════════
//  订单相关 API
// ═══════════════════════════════════════════

/** 创建订单（从购物车结算） */
export async function createOrder(orderData) {
  return postJson(`${API_BASE}/orders`, orderData);
}

/**
 * 查询当前用户的订单（支持多条件搜索过滤）
 * @param {{ start?: string, end?: string, bookTitle?: string }} params
 *   start/end: 时间范围（ISO 格式）
 *   bookTitle: 按书名过滤
 */
export async function getMyOrders(params = {}) {
  const q = new URLSearchParams();
  if (params.start) q.append('start', params.start);
  if (params.end) q.append('end', params.end);
  if (params.bookTitle) q.append('bookTitle', params.bookTitle);
  const qs = q.toString();
  return getJson(`${API_BASE}/orders${qs ? '?' + qs : ''}`);
}

/**
 * 管理员：查询所有用户的订单（支持搜索过滤）
 * @param {{ start?: string, end?: string, bookTitle?: string }} params
 */
export async function getAllOrders(params = {}) {
  const q = new URLSearchParams();
  if (params.start) q.append('start', params.start);
  if (params.end) q.append('end', params.end);
  if (params.bookTitle) q.append('bookTitle', params.bookTitle);
  const qs = q.toString();
  return getJson(`${API_BASE}/orders/all${qs ? '?' + qs : ''}`);
}

/** 查询单个订单详情 */
export async function getOrderById(id) {
  return getJson(`${API_BASE}/order/${id}`);
}

// ═══════════════════════════════════════════
//  统计相关 API
// ═══════════════════════════════════════════

/** 管理员：获取热销榜（按销量排序） */
export async function getSalesRanking(start, end) {
  const q = new URLSearchParams();
  if (start) q.append('start', start);
  if (end) q.append('end', end);
  const qs = q.toString();
  return getJson(`${API_BASE}/statistics/sales${qs ? '?' + qs : ''}`);
}

/** 管理员：获取消费榜（按金额排序） */
export async function getConsumptionRanking(start, end) {
  const q = new URLSearchParams();
  if (start) q.append('start', start);
  if (end) q.append('end', end);
  const qs = q.toString();
  return getJson(`${API_BASE}/statistics/consumption${qs ? '?' + qs : ''}`);
}

/** 顾客：获取个人购买统计 */
export async function getMyStatistics(start, end) {
  const q = new URLSearchParams();
  if (start) q.append('start', start);
  if (end) q.append('end', end);
  const qs = q.toString();
  return getJson(`${API_BASE}/statistics/my-purchases${qs ? '?' + qs : ''}`);
}