package com.reins.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_tbl")
@Data
@NoArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String address;

	private String receiver;

	private String tel;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
