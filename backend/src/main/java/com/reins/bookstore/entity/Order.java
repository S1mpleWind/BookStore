package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order 实体类映射数据库中的 order_tbl 表
 * 记录订单的头信息，包括收货地址、联系人及下单时间
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

	@Column(name = "user_id")
	private Long userId;     // 下单用户的 ID

	@Column(name = "created_at")
	private LocalDateTime createdAt; // 订单创建时间
}
