package com.example.TestNodo.controller;

import com.example.TestNodo.dto.CreateOrderDTO;
import com.example.TestNodo.entity.Order;
import com.example.TestNodo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderDTO dto) {
        Order saved = orderService.createOrder(dto);
        return ResponseEntity.ok(saved);
    }

}
