package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.entity.Book;
import com.reins.bookstore.repository.BookRepository;
import com.reins.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Spring in Action");
        book1.setAuthor("Craig Walls");
        book1.setCover("spring-cover.jpg");
        book1.setPrice(59.99);
        book1.setDesc("A comprehensive guide to Spring");
        book1.setInventory(50);
        book1.setPublisher("Manning");
        book1.setIsbn("978-1617294945");
        book1.setSales(100);
        book1.setRecommended(true);

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Clean Code");
        book2.setAuthor("Robert C. Martin");
        book2.setCover("cleancode-cover.jpg");
        book2.setPrice(49.99);
        book2.setDesc("A handbook of agile software craftsmanship");
        book2.setInventory(30);
        book2.setPublisher("Prentice Hall");
        book2.setIsbn("978-0132350884");
        book2.setSales(80);
        book2.setRecommended(false);
    }

    @Test
    void findAll_shouldReturnAllBooksAsDTOs() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Spring in Action", result.get(0).getTitle());
        assertEquals("Clean Code", result.get(1).getTitle());
        assertEquals(Integer.valueOf(59), result.get(0).getPrice()); // 59.99.intValue() = 59
        assertEquals(50, result.get(0).getInventory());
        assertEquals("978-1617294945", result.get(0).getIsbn());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoBooks() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<BookDTO> result = bookService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnBookDTOWhenFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Optional<BookDTO> result = bookService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Spring in Action", result.get().getTitle());
        assertEquals("Craig Walls", result.get().getAuthor());
        assertEquals(Boolean.TRUE, result.get().getRecommended());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<BookDTO> result = bookService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void searchByTitle_shouldReturnMatchingBooks() {
        when(bookRepository.findByTitleContainingIgnoreCase("spring")).thenReturn(Arrays.asList(book1));

        List<BookDTO> result = bookService.searchByTitle("spring");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Spring in Action", result.get(0).getTitle());
    }

    @Test
    void searchByTitle_shouldReturnAllWhenTitleIsNull() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.searchByTitle(null);

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void searchByTitle_shouldReturnAllWhenTitleIsEmpty() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.searchByTitle("   ");

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void searchByTitle_shouldReturnEmptyWhenNoMatch() {
        when(bookRepository.findByTitleContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());

        List<BookDTO> result = bookService.searchByTitle("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveBook_shouldSaveAndReturnDTO() {
        BookDTO dto = new BookDTO();
        dto.setTitle("New Book");
        dto.setAuthor("New Author");
        dto.setCover("new-cover.jpg");
        dto.setPrice(3999);
        dto.setDescription("A brand new book");
        dto.setInventory(20);
        dto.setPublisher("New Publisher");
        dto.setIsbn("978-0000000001");

        Book savedBook = new Book();
        savedBook.setId(3L);
        savedBook.setTitle("New Book");
        savedBook.setAuthor("New Author");
        savedBook.setCover("new-cover.jpg");
        savedBook.setPrice(39.99);
        savedBook.setDesc("A brand new book");
        savedBook.setInventory(20);
        savedBook.setPublisher("New Publisher");
        savedBook.setIsbn("978-0000000001");
        savedBook.setSales(0);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDTO result = bookService.saveBook(dto);

        assertNotNull(result);
        assertEquals("New Book", result.getTitle());
        assertEquals(Integer.valueOf(39), result.getPrice()); // 39.99.intValue() = 39
        assertEquals(20, result.getInventory());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void saveBook_shouldSetDefaultInventoryWhenNull() {
        BookDTO dto = new BookDTO();
        dto.setTitle("Book Without Inventory");
        dto.setAuthor("Author");
        dto.setPrice(1999);

        Book savedBook = new Book();
        savedBook.setId(4L);
        savedBook.setTitle("Book Without Inventory");
        savedBook.setInventory(100);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDTO result = bookService.saveBook(dto);

        assertNotNull(result);
        assertEquals(100, result.getInventory()); // 2 * 100 = 200
    }

    @Test
    void saveBook_shouldHandleNullPrice() {
        BookDTO dto = new BookDTO();
        dto.setTitle("Free Book");
        dto.setAuthor("Author");
        dto.setPrice(null);

        Book savedBook = new Book();
        savedBook.setId(5L);
        savedBook.setTitle("Free Book");
        savedBook.setPrice(null);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDTO result = bookService.saveBook(dto);

        assertNotNull(result);
        assertNull(result.getPrice());
    }

    @Test
    void updateBook_shouldUpdateExistingBook() {
        BookDTO dto = new BookDTO();
        dto.setTitle("Updated Title");
        dto.setPrice(2999);
        dto.setInventory(60);

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Craig Walls");
        updatedBook.setCover("spring-cover.jpg");
        updatedBook.setPrice(29.99);
        updatedBook.setDesc("A comprehensive guide to Spring");
        updatedBook.setInventory(60);
        updatedBook.setPublisher("Manning");
        updatedBook.setIsbn("978-1617294945");
        updatedBook.setSales(100);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        BookDTO result = bookService.updateBook(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(Integer.valueOf(29), result.getPrice()); // 29.99.intValue() = 29
        assertEquals(60, result.getInventory());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_shouldReturnNullWhenBookNotFound() {
        BookDTO dto = new BookDTO();
        dto.setTitle("Won't work");

        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        BookDTO result = bookService.updateBook(999L, dto);

        assertNull(result);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_shouldOnlyUpdateNonNullFields() {
        BookDTO dto = new BookDTO();
        dto.setTitle("New Title Only");
        // Other fields are null - should keep original values

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("New Title Only");
        updatedBook.setAuthor("Craig Walls");
        updatedBook.setPrice(59.99);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        BookDTO result = bookService.updateBook(1L, dto);

        assertNotNull(result);
        assertEquals("New Title Only", result.getTitle());
        assertEquals("Craig Walls", result.getAuthor());
        assertEquals(Integer.valueOf(59), result.getPrice()); // 59.99.intValue() = 59
    }

    @Test
    void deleteBook_shouldDeleteWhenExists() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        Map<String, Object> result = bookService.deleteBook(1L);

        assertNotNull(result);
        assertEquals("删除成功", result.get("message"));
        assertNull(result.get("error"));
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBook_shouldReturnErrorWhenNotExists() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        Map<String, Object> result = bookService.deleteBook(999L);

        assertNotNull(result);
        assertEquals("书籍不存在", result.get("error"));
        assertNull(result.get("message"));
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void toDTO_shouldHandleNullPrice() {
        book1.setPrice(null);
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1));

        List<BookDTO> result = bookService.findAll();

        assertNull(result.get(0).getPrice());
    }
}