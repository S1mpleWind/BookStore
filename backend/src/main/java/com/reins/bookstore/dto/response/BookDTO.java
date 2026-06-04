package com.reins.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookDTO 是书籍信息的响应 DTO
 * 只暴露前端需要的字段，隐藏数据库内部细节
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String cover;
    private Integer price;       // 价格（单位：分），避免浮点数精度问题
    private String description;  // 书籍描述
    private Boolean recommended; // 是否推荐
}