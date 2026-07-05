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

/**
 * 书籍控制器
 *
 * 处理书籍的查询和 CRUD 操作。
 * 所有用户（顾客和管理员）都可以查看书籍列表和详情，
 * 但新增、修改、删除操作仅管理员可用。
 *
 * 权限规则：
 * - GET /book 和 GET /book/{id} → 所有登录用户可用
 * - POST/PUT/DELETE /book → 仅管理员（identity == 1）
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 获取书籍列表（支持按书名搜索）
     *
     * 如果传了 title 参数 → 模糊搜索（忽略大小写）
     * 如果没传 title 参数 → 返回全部书籍
     *
     * @param title 可选，书名搜索关键词
     * @return List<BookDTO>
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
     *
     * @param id 书籍 ID
     * @return BookDTO（含标题、作者、封面、价格、描述、库存、出版社、ISBN）
     */
    @GetMapping("/book/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 管理员：新增书籍
     *
     * @param dto     书籍信息（标题、作者、封面、价格、库存、出版社、ISBN、描述）
     * @param request 用于校验管理员身份
     * @return 新增后的 BookDTO
     */
    @PostMapping("/book")
    public ResponseEntity<?> addBook(@RequestBody BookDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(bookService.saveBook(dto));
    }

    /**
     * 管理员：修改书籍
     *
     * 只更新 DTO 中非空的字段，其他字段保留原值。
     *
     * @param id      要修改的书籍 ID
     * @param dto     新的书籍信息（只传需要修改的字段即可）
     * @param request 用于校验管理员身份
     * @return 修改后的 BookDTO 或 404
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
     *
     * @param id      要删除的书籍 ID
     * @param request 用于校验管理员身份
     * @return { message: "删除成功" } 或 { error: "书籍不存在" }
     */
    @DeleteMapping("/book/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    /**
     * 校验当前用户是否为管理员
     *
     * 从 Session 中取出 identity 属性：
     * - identity == 1 → 管理员
     * - 其他值或 null → 非管理员
     *
     * @param request HTTP 请求（内含 Session）
     * @return true 如果是管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer identity = (Integer) session.getAttribute("identity");
        return identity != null && identity == 1;
    }
}