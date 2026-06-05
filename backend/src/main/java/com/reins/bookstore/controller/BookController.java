package com.reins.bookstore.controller;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 获取所有书籍列表 / 搜索
     */
    @GetMapping("/book")
    public List<BookDTO> getAllBooks(@RequestParam(required = false) String title) {
        if (title != null && !title.trim().isEmpty()) {
            return bookService.searchByTitle(title);
        }
        return bookService.findAll();
    }

    /**
     * 获取单本书籍详情
     */
    @GetMapping("/book/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 管理员：新增书籍
     */
    @PostMapping("/book")
    public ResponseEntity<?> addBook(@RequestBody BookDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(bookService.saveBook(dto));
    }

    /**
     * 管理员：修改书籍
     */
    @PutMapping("/book/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        BookDTO result = bookService.updateBook(id, dto);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    /**
     * 管理员：删除书籍
     */
    @DeleteMapping("/book/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer identity = (Integer) session.getAttribute("identity");
        return identity != null && identity == 1;
    }
}