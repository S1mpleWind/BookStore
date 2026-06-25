package com.reins.bookstore.repository;

import com.reins.bookstore.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 通过 JPA 关联路径查询：根据 User 实体的 ID 查找购物车项
     * Spring Data JPA 解析 findByUser_Id → user.id
     */
    List<Cart> findByUser_Id(Long userId);

    /**
     * 删除指定用户的所有购物车项
     * 通过关联路径 user.id 匹配
     */
    void deleteByUser_Id(Long userId);
}