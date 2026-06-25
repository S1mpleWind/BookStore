package com.reins.bookstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderItem 实体类映射数据库中的 order_item 表
 * 记录订单中每本书的购买详情
 * 与 Order 为多对一关联关系：多个订单项从属于同一个订单
 */
@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer number;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "book_title")
    private String bookTitle;

    @Column(name = "book_cover")
    private String bookCover;

    @Column(name = "unit_price")
    private Integer unitPrice;

    /**
     * 多对一关联：多个 OrderItem 属于同一个 Order
     * 外键列名为 order_id
     * cascade 不在此端定义，由 Order 端统一管理
     * fetch = LAZY：延迟加载，访问时才查询
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore  // 避免序列化时产生循环引用
    private Order order;
}