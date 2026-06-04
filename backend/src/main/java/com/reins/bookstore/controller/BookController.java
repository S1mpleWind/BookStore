package com.reins.bookstore.controller;

import com.reins.bookstore.dto.response.BookDTO;
import com.reins.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookController 是书籍模块的控制层
 * 负责接收前端关于书籍列表和书籍详情的 HTTP 请求
 */
@RestController             // controller + response body
@RequestMapping("/api/v1")  //该controller内所有api的前缀
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // enable CORS
public class BookController {

    @Autowired //IoC annotation
    private BookService bookService;

    /**
     * 获取所有书籍列表
     * @return 包含所有书籍对象的列表（BookDTO，而非 Entity）
     */
    @GetMapping("/book")
    public List<BookDTO> getAllBooks() {
        return bookService.findAll();
    }

    /**
     * 根据 ID 获取单本书籍的详情
     * @param id 书籍的唯一标识
     * @return 如果找到则返回书籍对象及 200 状态码，否则返回 404
     */
    @GetMapping("/book/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
