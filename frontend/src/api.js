// 前后端之间通过异步进行通信

/**
 * 封装通用的 POST 请求函数，前后端之间传递JSON数据
 */
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
    if (typeof data === 'object' && data.error) {
      throw new Error(JSON.stringify({ status: response.status, error: data.error, data }));
    }
    throw new Error(JSON.stringify({ status: response.status, data }));
  }

  return data;
}

/**
 * 获取所有书籍列表
 */
export async function getBooks() {
  // 使用默认的 GET
  const response = await fetch('http://localhost:8080/api/v1/book');
  if (!response.ok) throw new Error('Failed to fetch books');
  return await response.json();
}

/**
 * 获取指定 ID 的书籍详情
 */
export async function getBookById(id) {
  const response = await fetch(`http://localhost:8080/api/v1/book/${id}`);
  if (!response.ok) throw new Error('Failed to fetch book detail');
  return await response.json();
}

/**
 * 用户登录
 */
export async function loginUser(credentials) {
  return postJson('http://localhost:8080/api/v1/users/login', credentials);
}

/**
 * 用户注册
 */
export async function registerUser(profile) {
  return postJson('http://localhost:8080/api/v1/users/register', profile);
}

/**
 * 获取用户的购物车信息
 */
export async function getCart(userId) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/${userId}`);
  if (!response.ok) throw new Error('Failed to fetch cart');
  return await response.json();
}

/**
 * 添加书籍到购物车
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
 * 删除指定的购物车项
 */
export async function deleteCartItem(cartItemId) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/${cartItemId}`, {
    method: 'DELETE'
  });
  if (!response.ok) throw new Error('Failed to delete cart item');
  return await response.json();
}

/**
 * 清空指定用户的购物车
 */
export async function clearCart(userId) {
  const response = await fetch(`http://localhost:8080/api/v1/cart/user/${userId}`, {
    method: 'DELETE'
  });
  if (!response.ok) throw new Error('Failed to clear cart');
  return await response.json();
}

/**
 * 获取用户的历史订单列表
 */
export async function getOrders(userId) {
  const response = await fetch(`http://localhost:8080/api/v1/orders/${userId}`);
  if (!response.ok) throw new Error('Failed to fetch orders');
  return await response.json();
}

/**
 * 创建新订单（下单结算）
 */
export async function createOrder(orderData) {
  return postJson('http://localhost:8080/api/v1/orders', orderData);
}

