package com.reins.bookstore.service.impl;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    private BookDTO toDTO(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCover(),
                book.getPrice() == null ? null : book.getPrice().intValue(),
                book.getDesc(),
                book.getRecommended(),
                book.getInventory(),
                book.getPublisher(),
                book.getIsbn()
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
        return bookRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<BookDTO> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return findAll();
        }
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookDTO saveBook(BookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setCover(dto.getCover());
        book.setPrice(dto.getPrice() != null ? dto.getPrice().doubleValue() : null);
        book.setDesc(dto.getDescription());
        book.setInventory(dto.getInventory() != null ? dto.getInventory() : 100);
        book.setPublisher(dto.getPublisher());
        book.setIsbn(dto.getIsbn());
        book.setSales(0);
        return toDTO(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookDTO dto) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return null;
        if (dto.getTitle() != null) book.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
        if (dto.getCover() != null) book.setCover(dto.getCover());
        if (dto.getPrice() != null) book.setPrice(dto.getPrice().doubleValue());
        if (dto.getDescription() != null) book.setDesc(dto.getDescription());
        if (dto.getInventory() != null) book.setInventory(dto.getInventory());
        if (dto.getPublisher() != null) book.setPublisher(dto.getPublisher());
        if (dto.getIsbn() != null) book.setIsbn(dto.getIsbn());
        return toDTO(bookRepository.save(book));
    }

    @Override
    @Transactional
    public Map<String, Object> deleteBook(Long id) {
        Map<String, Object> res = new HashMap<>();
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            res.put("message", "删除成功");
        } else {
            res.put("error", "书籍不存在");
        }
        return res;
    }
}