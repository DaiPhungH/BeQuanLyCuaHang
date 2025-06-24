package com.example.TestNodo.repository;

import com.example.TestNodo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> { }

