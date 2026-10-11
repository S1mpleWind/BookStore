import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AUTH_CHANGED, clearAuth, getToken, saveToken } from '../auth';
import { getBookById, loginUser, logoutUser } from '../api';

describe('JWT request lifecycle', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn());
  });

  it('stores the login token and sends it on subsequent requests', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      headers: { get: () => 'application/json' },
      json: async () => ({ userId: 1, token: 'signed.jwt.token' }),
    });
    await loginUser({ username: 'demo', password: 'demo123' });
    expect(getToken()).toBe('signed.jwt.token');
    fetch.mockResolvedValueOnce({ ok: true, json: async () => ({ id: 1 }) });
    await getBookById(1);
    expect(fetch).toHaveBeenLastCalledWith('http://localhost:8080/api/v1/books/1', {
      headers: { Authorization: 'Bearer signed.jwt.token' },
    });
  });

  it('clears stale credentials and notifies the UI on a protected request 401', async () => {
    saveToken('expired.jwt.token');
    localStorage.setItem('user', '{"userId":1}');
    localStorage.setItem('isLoggedIn', 'true');
    const listener = vi.fn();
    window.addEventListener(AUTH_CHANGED, listener);
    try {
      fetch.mockResolvedValue({ ok: false, status: 401 });
      await expect(getBookById(1)).rejects.toThrow();
      expect(getToken()).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
      expect(listener).toHaveBeenCalledOnce();
    } finally {
      window.removeEventListener(AUTH_CHANGED, listener);
    }
  });

  it('clears local credentials even when the logout request fails', async () => {
    saveToken('signed.jwt.token');
    fetch.mockRejectedValue(new Error('offline'));
    await expect(logoutUser()).rejects.toThrow('offline');
    expect(getToken()).toBeNull();
  });

  it('does not clear credentials for a forbidden operation', async () => {
    saveToken('signed.jwt.token');
    fetch.mockResolvedValue({ ok: false, status: 403 });
    await expect(getBookById(1)).rejects.toThrow();
    expect(getToken()).toBe('signed.jwt.token');
    clearAuth();
  });
});
