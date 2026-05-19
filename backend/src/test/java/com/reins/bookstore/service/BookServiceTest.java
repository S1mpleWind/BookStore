package com.reins.bookstore.service;

import com.reins.bookstore.entity.Book;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    public void testFindAll() {
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Test Book 1");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Test Book 2");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> books = bookService.findAll();
        assertEquals(2, books.size());
        assertEquals("Test Book 1", books.get(0).getTitle());
    }

    @Test
    public void testFindById() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book 1");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = bookService.findById(1L);
        assertTrue(foundBook.isPresent());
        assertEquals("Test Book 1", foundBook.get().getTitle());
    }
}
