package com.reins.bookstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order 实体类映射数据库中的 order_tbl 表
 * 记录订单的头信息，包括收货地址、联系人及下单时间
 * 与 OrderItem 为一对多级联关联关系，与 User 为多对一关联关系
 */
@Entity
@Table(name = "order_tbl")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;  // 收货地址

    private String receiver; // 收货人姓名

    private String tel;      // 联系电话

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 订单创建时间

    /**
     * 多对一关联：多个订单从属于同一个用户
     * 外键列名为 user_id
     * fetch = LAZY：延迟加载，仅在访问时查询关联对象
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    /**
     * 一对多关联：一个订单包含多个订单项
     * cascade = CascadeType.ALL：对 Order 的持久化操作（persist/merge/remove）会级联到 OrderItem
     * orphanRemoval = true：从 items 集合中移除的 OrderItem 会被自动删除
     * mappedBy = "order"：由 OrderItem 中的 order 字段维护关联关系，Order 端为 inverse
     * fetch = LAZY：延迟加载
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * 便捷获取用户 ID（通过关联实体导航）
     * 保持与之前代码的兼容性
     */
    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * 便捷方法：向订单中添加订单项，自动维护双向关联关系
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * 便捷方法：从订单中移除订单项，自动维护双向关联关系
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}