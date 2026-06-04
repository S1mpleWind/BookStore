package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.BookDTO;
import java.util.List;
import java.util.Optional;

/**
 * BookService 接口定义了书籍相关的业务逻辑
 * 返回 BookDTO 而非 Entity，实现数据层与表现层解耦
 */
public interface BookService {
    /**
     * 获取所有书籍
     */
    List<BookDTO> findAll();

    /**
     * 根据 ID 查找书籍
     */
    Optional<BookDTO> findById(Long id);
}
