package com.reins.bookstore.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
//bean that handles HTTP request
public class RootController implements ErrorController {

    @GetMapping("/")
    public String root() {
        // 开发时把根路径重定向到前端
        return "redirect:http://localhost:5173";
    }

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        return ResponseEntity.status(status != null ? status : 500)
                .body(Map.of(
                        "status", status != null ? status : 500,
                        "error", message != null ? message : "Not Found",
                        "path", request.getAttribute("jakarta.servlet.error.request_uri")
                ));
    }

}
