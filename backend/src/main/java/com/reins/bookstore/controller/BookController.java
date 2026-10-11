package com.reins.bookstore.controller;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping({"/books", "/book"})
    public List<BookDTO> getAllBooks(@RequestParam(required = false) String title) {
        if (title != null && !title.trim().isEmpty()) {
            return bookService.searchByTitle(title);
        }
        return bookService.findAll();
    }

    @GetMapping({"/books/{id}", "/book/{id}"})
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        // 记录处理真实 HTTP 请求的线程，作为虚拟线程验证证据。
        boolean virtual = Thread.currentThread().isVirtual();
        org.slf4j.LoggerFactory.getLogger(BookController.class)
                .info("GET book/{} virtual={}", id, virtual);
        return bookService.findById(id)
                .map(book -> ResponseEntity.ok().header("X-Virtual-Thread", Boolean.toString(virtual)).body(book))
                .orElse(ResponseEntity.notFound().header("X-Virtual-Thread", Boolean.toString(virtual)).build());
    }

    @PostMapping({"/books", "/book"})
    public ResponseEntity<?> addBook(@RequestBody BookDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        BookDTO saved = bookService.saveBook(dto);
        return ResponseEntity.created(java.net.URI.create("/api/v1/books/" + saved.getId())).body(saved);
    }

    @RequestMapping(value = {"/books/{id}", "/book/{id}"}, method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        BookDTO result = bookService.updateBook(id, dto);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping({"/books/{id}", "/book/{id}"})
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        Map<String, Object> result = bookService.deleteBook(id);
        return ResponseEntity.status(result.containsKey("error") ? 404 : 200).body(result);
    }

    private boolean isAdmin(HttpServletRequest request) {
        Integer identity = (Integer) request.getAttribute("identity");
        return identity != null && identity == 1;
    }
}