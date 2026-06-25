package com.reins.bookstore.repository;

import com.reins.bookstore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 通过 JPA 关联路径查询：根据 User 实体的 ID 查找订单
     * Spring Data JPA 会自动解析 findByUserId → user.id
     */
    List<Order> findByUser_Id(Long userId);

    List<Order> findByUser_IdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 通过 JPQL 查询：使用关联路径 o.user.id 导航到 User 实体的 ID
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.createdAt BETWEEN :start AND :end")
    List<Order> findByUserIdAndTimeRange(@Param("userId") Long userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}