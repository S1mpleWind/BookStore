package com.reins.bookstore.service.impl;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BookServiceImpl 是 BookService 接口的实现类
 * 负责将 Entity 转换为 DTO，实现数据层与表现层解耦
 */
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    /**
     * 将 Book Entity 转换为 BookDTO
     */
    private BookDTO toDTO(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCover(),
                book.getPrice() == null ? null : book.getPrice().intValue(),
                book.getDesc(),
                book.getRecommended()
        );
    }

    @Override
    public List<BookDTO> findAll() {
        return bookRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookDTO> findById(Long id) {
        return bookRepository.findById(id)
                .map(this::toDTO);
    }
}
