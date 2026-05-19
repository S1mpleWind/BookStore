import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getBooks, getBookById } from '../api';

// Mock global fetch
global.fetch = vi.fn();

describe('api.js', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  it('getBooks should fetch and return data', async () => {
    const mockBooks = [{ id: 1, title: 'Book 1' }];
    fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockBooks),
    });

    const result = await getBooks();
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/book');
    expect(result).toEqual(mockBooks);
  });

  it('getBookById should fetch and return data', async () => {
    const mockBook = { id: 1, title: 'Book 1' };
    fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockBook),
    });

    const result = await getBookById(1);
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/book/1');
    expect(result).toEqual(mockBook);
  });

  it('getBookById should throw error when response is not ok', async () => {
    fetch.mockResolvedValue({
      ok: false,
    });

    await expect(getBookById(999)).rejects.toThrow('Failed to fetch book detail');
  });

  it('loginUser should post credentials and return user info', async () => {
    const mockCredentials = { username: 'test', password: 'password' };
    const mockResponse = { userId: 1, username: 'test' };
    
    fetch.mockResolvedValue({
      ok: true,
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve(mockResponse),
    });

    const result = await loginUser(mockCredentials);
    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/users/login',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(mockCredentials)
      })
    );
    expect(result).toEqual(mockResponse);
  });
});
