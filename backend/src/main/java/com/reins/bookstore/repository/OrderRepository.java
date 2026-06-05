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
    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :start AND :end")
    List<Order> findByUserIdAndTimeRange(@Param("userId") Long userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}