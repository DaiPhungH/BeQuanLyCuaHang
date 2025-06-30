package com.example.TestNodo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private LocalDateTime orderDate;

    private Double totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(Long id, String customerName, LocalDateTime orderDate, Double totalAmount, List<OrderItem> items) {
        this.id = id;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    // ====== Getter / Setter ======

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }

    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getOrderDate() { return orderDate; }

    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Double getTotalAmount() { return totalAmount; }

    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderItem> getItems() { return items; }

    public void setItems(List<OrderItem> items) { this.items = items; }

    // ====== Logic mặc định nếu không nhập tên khách ======

    @PrePersist
    public void prePersist() {
        if (this.customerName == null || this.customerName.trim().isEmpty()) {
            this.customerName = "Ẩn danh";
        }
    }
}
