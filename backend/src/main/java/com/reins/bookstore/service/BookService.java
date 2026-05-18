package com.reins.bookstore.service;

import com.reins.bookstore.entity.Book;
import java.util.List;
import java.util.Optional;

/**
 * BookService 接口定义了书籍相关的业务逻辑
 */
public interface BookService {
    /**
     * 获取所有书籍
     */
    List<Book> findAll();

    /**
     * 根据 ID 查找书籍
     */
    Optional<Book> findById(Long id);
}
