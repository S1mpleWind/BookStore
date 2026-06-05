package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.BookDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookService {
    List<BookDTO> findAll();
    Optional<BookDTO> findById(Long id);
    List<BookDTO> searchByTitle(String title);
    BookDTO saveBook(BookDTO dto);
    BookDTO updateBook(Long id, BookDTO dto);
    Map<String, Object> deleteBook(Long id);
}