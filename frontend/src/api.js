/**
 * 后端接口占位模块（本地运行版）
 *
 * 说明：当前项目以本地交互为主。以下函数保留并执行本地 fallback 行为，同时提供
 * 注释说明建议的后端接口契约（URL、HTTP 方法、请求体与返回值）。当后端可用时，
 * 可将内部的 `fetch` 调用指向真实 API 并去除本地 fallback。
 */

/**
 * 保存/更新用户资料到后端
 *
 * API 设计建议：
 * POST /api/profile    -> 创建或更新当前用户资料
 * Request JSON: { name: string, email: string }
 * Response JSON: { id: string, name: string, email: string, updatedAt: string }
 *
 * @param {{name?:string,email?:string}} profile
 * @returns {Promise<object>} 解析为后端返回的用户对象；如果后端不可用则解析为输入的 profile
 */
export async function saveUserProfile(profile) {
  try {
    // 示例 fetch（生产中请替换为真实后端地址并处理鉴权 header）
    // const res = await fetch('/api/profile', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(profile),
    // });
    // if (!res.ok) throw new Error('Network response was not ok');
    // return await res.json();

    // 当前 fallback：本地直接返回并模拟后端字段
    return Promise.resolve({ id: 'local-1', ...profile, updatedAt: new Date().toISOString() });
  } catch (err) {
    return Promise.reject(err);
  }
}

/**
 * 从后端获取所有书籍列表
 */
//? modify the port num here if needed
export async function getBooks() {
  const response = await fetch('http://localhost:8080/api/v1/book');
  if (!response.ok) throw new Error('Failed to fetch books');
  return await response.json();
}

/**
 * 从后端获取指定 ID 的书籍详情
 */
export async function getBookById(id) {
  const response = await fetch(`http://localhost:8080/api/v1/book/${id}`);
  if (!response.ok) throw new Error('Failed to fetch book detail');
  return await response.json();
}

async function postJson(url, payload) {
  console.debug('[api] POST', url, payload);
  const response = await fetch(url, {
    method: 'POST',
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

  console.debug('[api] response', url, {
    ok: response.ok,
    status: response.status,
    contentType,
    data,
  });

  if (!response.ok) {
    // 如果返回的是JSON对象且包含error字段，抛出error消息
    if (typeof data === 'object' && data.error) {
      throw new Error(JSON.stringify({ status: response.status, error: data.error, data }));
    }
    // 否则抛出通用错误消息，包含状态码与返回体，便于前端调试
    throw new Error(JSON.stringify({ status: response.status, data }));
  }

  return data;
}

export async function loginUser(credentials) {
  return postJson('http://localhost:8080/api/v1/users/login', credentials);
}

export async function registerUser(profile) {
  return postJson('http://localhost:8080/api/v1/users/register', profile);
}

/**
 * 获取用户的购物车
 */
export async function getCart(userId) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/${userId}`);
  if (!response.ok) throw new Error('Failed to fetch cart');
  return await response.json();
}

/**
 * 添加商品到购物车
 */
export async function addToCart(userId, bookId, quantity = 1) {
  return postJson('http://localhost:8080/api/v1/cart', {
    userId,
    bookId,
    quantity
  });
}

/**
 * 更新购物车项的数量
 */
export async function updateCartItem(cartItemId, quantity) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/${cartItemId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ quantity })
  });
  if (!response.ok) throw new Error('Failed to update cart item');
  return await response.json();
}

/**
 * 删除购物车项
 */
export async function deleteCartItem(cartItemId) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/${cartItemId}`, {
    method: 'DELETE'
  });
  if (!response.ok) throw new Error('Failed to delete cart item');
  return await response.json();
}

/**
 * 清空购物车
 */
export async function clearCart(userId) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/user/${userId}`, {
    method: 'DELETE'
  });
  if (!response.ok) throw new Error('Failed to clear cart');
  return await response.json();
}

/**
 * 获取用户的订单列表
 */
export async function getOrders(userId) {
  console.debug('[api] GET orders', userId);
  const response = await fetch(`http://localhost:8080/api/v1/orders/${userId}`);
  if (!response.ok) throw new Error('Failed to fetch orders');
  const data = await response.json();
  console.debug('[api] GET orders response', data);
  return data;
}

/**
 * 创建订单
 */
export async function createOrder(orderData) {
  return postJson('http://localhost:8080/api/v1/orders', orderData);
}

/**
 * 将商品加入用户的远端购物车（非必需）
 *
 * API 设计建议：
 * POST /api/cart/add
 * Request JSON: { userId: string, bookId: number, quantity?: number }
 * Response JSON: { success: true, cartCount: number }
 *
 * @param {{userId?:string}} userInfo
 * @param {{id:number}} book
 * @param {number} quantity
 */
export async function addToCartRemote(userInfo, book, quantity = 1) {
  try {
    // const res = await fetch('/api/cart/add', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: userInfo?.id, bookId: book.id, quantity }) });
    // if (!res.ok) throw new Error('Network error');
    // return await res.json();
    return Promise.resolve({ success: true, cartCount: (Math.floor(Math.random() * 5) + quantity) });
  } catch (err) {
    return Promise.reject(err);
  }
}

/**
 * 直接购买（创建订单）
 *
 * API 设计建议：
 * POST /api/purchase
 * Request JSON: { userId: string, items: [{ bookId: number, quantity: number }], paymentMethod?: string }
 * Response JSON: { orderId: string, status: 'created'|'failed', amount: number }
 *
 * @param {{userId?:string}} userInfo
 * @param {{id:number, price:number}} book
 * @param {number} quantity
 */
export async function purchaseNow(userInfo, book, quantity = 1) {
  try {
    // const res = await fetch('/api/purchase', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: userInfo?.id, items: [{ bookId: book.id, quantity }], paymentMethod: 'mock' }) });
    // if (!res.ok) throw new Error('Purchase failed');
    // return await res.json();
    return Promise.resolve({ orderId: `local-${Date.now()}`, status: 'created', amount: (book.price || 0) * quantity });
  } catch (err) {
    return Promise.reject(err);
  }
}
