/* global global */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  getBooks,
  getBookById,
  addBook,
  updateBook,
  deleteBook,
  loginUser,
  registerUser,
  logoutUser,
  getUsersList,
  toggleUserStatus,
  getCart,
  addToCart,
  updateCartItem,
  deleteCartItem,
  clearCart,
  createOrder,
  getMyOrders,
  getAllOrders,
  getOrderById,
  getSalesRanking,
  getConsumptionRanking,
  getMyStatistics,
} from '../api';

// Mock global fetch
global.fetch = vi.fn();

describe('api.js', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  // ========== BOOKS ==========

  describe('getBooks', () => {
    it('should fetch all books without title filter', async () => {
      const mockBooks = [{ id: 1, title: 'Book 1' }, { id: 2, title: 'Book 2' }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockBooks),
      });

      const result = await getBooks();
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/book', { credentials: 'include' });
      expect(result).toEqual(mockBooks);
    });

    it('should fetch books filtered by title', async () => {
      const mockBooks = [{ id: 1, title: 'Spring' }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockBooks),
      });

      const result = await getBooks('Spring');
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/book?title=Spring', { credentials: 'include' });
      expect(result).toEqual(mockBooks);
    });

    it('should URL-encode special characters in title', async () => {
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve([]),
      });

      await getBooks('C# Programming');
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/book?title=C%23%20Programming', { credentials: 'include' });
    });

    it('should throw on non-ok response', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 500,
      });

      await expect(getBooks()).rejects.toThrow();
    });
  });

  describe('getBookById', () => {
    it('should fetch book by id', async () => {
      const mockBook = { id: 1, title: 'Book 1' };
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockBook),
      });

      const result = await getBookById(1);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/book/1', { credentials: 'include' });
      expect(result).toEqual(mockBook);
    });

    it('should throw on not found', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 404,
      });

      await expect(getBookById(999)).rejects.toThrow();
    });
  });

  describe('addBook', () => {
    it('should post book data and return created book', async () => {
      const bookData = { title: 'New Book', author: 'Author', price: 2999 };
      const mockResponse = { id: 3, ...bookData };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve(mockResponse),
      });

      const result = await addBook(bookData);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/book',
        expect.objectContaining({
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(bookData),
        })
      );
      expect(result).toEqual(mockResponse);
    });

    it('should throw on forbidden', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 403,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ error: '无权限' }),
      });

      await expect(addBook({ title: 'Book' })).rejects.toThrow();
    });
  });

  describe('updateBook', () => {
    it('should put updated book data', async () => {
      const updates = { title: 'Updated Title', price: 3999 };
      const mockResponse = { id: 1, ...updates };
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await updateBook(1, updates);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/book/1',
        expect.objectContaining({
          method: 'PUT',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updates),
        })
      );
      expect(result).toEqual(mockResponse);
    });

    it('should throw on failure', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 404,
        json: () => Promise.resolve({}),
      });

      await expect(updateBook(999, { title: 'Nope' })).rejects.toThrow();
    });
  });

  describe('deleteBook', () => {
    it('should delete book and return response', async () => {
      const mockResponse = { message: '删除成功' };
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await deleteBook(1);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/book/1',
        { method: 'DELETE', credentials: 'include' }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should throw on failure', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 404,
        json: () => Promise.resolve({ error: '书籍不存在' }),
      });

      await expect(deleteBook(999)).rejects.toThrow();
    });
  });

  // ========== AUTH ==========

  describe('loginUser', () => {
    it('should post credentials and return user info', async () => {
      const credentials = { username: 'test', password: 'password' };
      const mockResponse = { userId: 1, username: 'test', nickname: 'Test', identity: 0 };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve(mockResponse),
      });

      const result = await loginUser(credentials);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/users/login',
        expect.objectContaining({
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(credentials),
        })
      );
      expect(result.userId).toBe(1);
      expect(result.username).toBe('test');
    });

    it('should throw on wrong credentials', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 401,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ error: 'Invalid credentials' }),
      });

      await expect(loginUser({ username: 'test', password: 'wrong' })).rejects.toThrow();
    });

    it('should handle non-json response body', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 500,
        headers: { get: () => 'text/plain' },
        text: () => Promise.resolve('Internal Server Error'),
      });

      await expect(loginUser({ username: 'test', password: 'pass' })).rejects.toThrow();
    });

    it('should handle response with no content-type', async () => {
      const mockResponse = { userId: 1 };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => '' },
        text: () => Promise.resolve(JSON.stringify(mockResponse)),
      });

      const result = await loginUser({ username: 'test', password: 'pass' });
      expect(result).toBe(JSON.stringify(mockResponse));
    });
  });

  describe('registerUser', () => {
    it('should post profile and return success', async () => {
      const profile = { username: 'newuser', password: 'pass', nickname: 'New', email: 'new@test.com' };
      const mockResponse = { message: '注册成功' };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve(mockResponse),
      });

      const result = await registerUser(profile);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/users/register',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(profile),
        })
      );
      expect(result.message).toBe('注册成功');
    });

    it('should throw on duplicate username', async () => {
      fetch.mockResolvedValue({
        ok: false,
        status: 409,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({ error: '用户名已存在' }),
      });

      await expect(registerUser({ username: 'exists', password: 'pass' })).rejects.toThrow();
    });
  });

  describe('logoutUser', () => {
    it('should post logout request', async () => {
      const mockResponse = { message: '已退出登录' };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve(mockResponse),
      });

      const result = await logoutUser();
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/users/logout',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({}),
        })
      );
      expect(result).toEqual(mockResponse);
    });
  });

  // ========== USERS (ADMIN) ==========

  describe('getUsersList', () => {
    it('should fetch users list', async () => {
      const mockUsers = [{ id: 1, username: 'admin' }, { id: 2, username: 'user1' }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockUsers),
      });

      const result = await getUsersList();
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/users/list', { credentials: 'include' });
      expect(result).toHaveLength(2);
    });

    it('should throw on forbidden', async () => {
      fetch.mockResolvedValue({ ok: false, status: 403 });
      await expect(getUsersList()).rejects.toThrow();
    });
  });

  describe('toggleUserStatus', () => {
    it('should toggle user status', async () => {
      const mockResponse = { message: '用户已禁用' };
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await toggleUserStatus(1);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/users/1/status',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({}),
        })
      );
      expect(result.message).toBe('用户已禁用');
    });
  });

  // ========== CART ==========

  describe('getCart', () => {
    it('should fetch user cart', async () => {
      const mockCart = [{ id: 1, bookId: 1, number: 2 }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockCart),
      });

      const result = await getCart(1);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/cart/1', { credentials: 'include' });
      expect(result).toHaveLength(1);
    });
  });

  describe('addToCart', () => {
    it('should add item to cart with default quantity', async () => {
      const mockResponse = { id: 1, bookId: 1, number: 1 };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve(mockResponse),
      });

      const result = await addToCart(1, 1);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/cart',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ userId: 1, bookId: 1, quantity: 1 }),
        })
      );
      expect(result.number).toBe(1);
    });

    it('should add item to cart with custom quantity', async () => {
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve({}),
      });

      await addToCart(1, 2, 5);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/cart',
        expect.objectContaining({
          body: JSON.stringify({ userId: 1, bookId: 2, quantity: 5 }),
        })
      );
    });
  });

  describe('updateCartItem', () => {
    it('should update cart item quantity', async () => {
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ id: 1, number: 3 }),
      });

      const result = await updateCartItem(1, 3);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/cart/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ quantity: 3 }),
        })
      );
      expect(result.number).toBe(3);
    });
  });

  describe('deleteCartItem', () => {
    it('should delete cart item', async () => {
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ message: '删除成功' }),
      });

      await deleteCartItem(1);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/cart/1',
        { method: 'DELETE', credentials: 'include' }
      );
    });
  });

  describe('clearCart', () => {
    it('should clear entire cart for user', async () => {
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ message: '购物车已清空' }),
      });

      await clearCart(1);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/cart/user/1',
        { method: 'DELETE', credentials: 'include' }
      );
    });
  });

  // ========== ORDERS ==========

  describe('createOrder', () => {
    it('should create order with addresses and return order data', async () => {
      const orderData = { receiver: 'John', address: 'Addr', tel: '123' };
      const mockOrder = { id: 1, ...orderData, items: [] };
      fetch.mockResolvedValue({
        ok: true,
        headers: { get: () => 'application/json' },
        json: () => Promise.resolve(mockOrder),
      });

      const result = await createOrder(orderData);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/orders',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(orderData),
        })
      );
      expect(result.id).toBe(1);
    });
  });

  describe('getMyOrders', () => {
    it('should fetch my orders without filters', async () => {
      const mockOrders = [{ id: 1, receiver: 'Me' }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockOrders),
      });

      const result = await getMyOrders();
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/orders', { credentials: 'include' });
      expect(result).toHaveLength(1);
    });

    it('should fetch my orders with time filters', async () => {
      fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) });

      await getMyOrders({ start: '2026-01-01', end: '2026-06-01' });
      const url = fetch.mock.calls[0][0];
      expect(url).toContain('start=');
      expect(url).toContain('end=');
    });

    it('should fetch my orders with bookTitle filter', async () => {
      fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) });

      await getMyOrders({ bookTitle: 'Spring' });
      expect(fetch.mock.calls[0][0]).toContain('bookTitle=Spring');
    });
  });

  describe('getAllOrders', () => {
    it('should fetch all orders', async () => {
      const mockOrders = [{ id: 1 }, { id: 2 }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockOrders),
      });

      const result = await getAllOrders();
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/orders/all', { credentials: 'include' });
      expect(result).toHaveLength(2);
    });

    it('should fetch all orders with filters', async () => {
      fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) });

      await getAllOrders({ start: '2026-01-01', end: '2026-06-01', bookTitle: 'Book' });
      const url = fetch.mock.calls[0][0];
      expect(url).toContain('start=');
      expect(url).toContain('end=');
      expect(url).toContain('bookTitle=');
    });
  });

  describe('getOrderById', () => {
    it('should fetch single order', async () => {
      const mockOrder = { id: 1, receiver: 'John', items: [] };
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockOrder),
      });

      const result = await getOrderById(1);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/order/1', { credentials: 'include' });
      expect(result.receiver).toBe('John');
    });
  });

  // ========== STATISTICS ==========

  describe('getSalesRanking', () => {
    it('should fetch sales ranking without filters', async () => {
      const mockRanking = [{ bookId: 1, bookTitle: 'Book 1', sales: 100 }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockRanking),
      });

      const result = await getSalesRanking();
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/statistics/sales',
        { credentials: 'include' }
      );
      expect(result[0].sales).toBe(100);
    });

    it('should fetch sales ranking with time range', async () => {
      fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) });

      await getSalesRanking('2026-01-01', '2026-06-01');
      const url = fetch.mock.calls[0][0];
      expect(url).toContain('start=2026-01-01');
      expect(url).toContain('end=2026-06-01');
    });
  });

  describe('getConsumptionRanking', () => {
    it('should fetch consumption ranking', async () => {
      const mockRanking = [{ userId: 1, nickname: 'User1', totalAmount: 5000 }];
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockRanking),
      });

      const result = await getConsumptionRanking();
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/statistics/consumption',
        { credentials: 'include' }
      );
      expect(result[0].totalAmount).toBe(5000);
    });
  });

  describe('getMyStatistics', () => {
    it('should fetch personal statistics', async () => {
      const mockStats = { totalBooks: 5, totalAmount: 15000, details: [] };
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockStats),
      });

      const result = await getMyStatistics();
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/statistics/my-purchases',
        { credentials: 'include' }
      );
      expect(result.totalBooks).toBe(5);
      expect(result.totalAmount).toBe(15000);
    });

    it('should fetch personal statistics with time range', async () => {
      fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve({}) });

      await getMyStatistics('2026-01-01', '2026-06-01');
      const url = fetch.mock.calls[0][0];
      expect(url).toContain('start=2026-01-01');
      expect(url).toContain('end=2026-06-01');
    });
  });

  // ========== EDGE CASES ==========

  describe('error handling', () => {
    it('should handle network errors gracefully', async () => {
      fetch.mockRejectedValue(new Error('Network error'));

      await expect(getBooks()).rejects.toThrow('Network error');
    });

    it('should handle unexpected JSON parse failures', async () => {
      fetch.mockResolvedValue({
        ok: true,
        json: () => Promise.reject(new Error('Invalid JSON')),
      });

      await expect(getBooks()).rejects.toThrow('Invalid JSON');
    });
  });
});