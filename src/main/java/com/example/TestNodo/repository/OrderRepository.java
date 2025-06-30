package com.example.TestNodo.repository;

import com.example.TestNodo.dto.OrderHistoryDTO;
import com.example.TestNodo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderDateDesc();

    @Query("""
                SELECT DISTINCT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product p
                WHERE (:name IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :name, '%')))
                  AND (:from IS NULL OR o.orderDate >= :from)
                  AND (:to IS NULL OR o.orderDate < :to)
                ORDER BY o.orderDate DESC
            """)
    List<Order> searchOrders(
            @Param("name") String name,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}