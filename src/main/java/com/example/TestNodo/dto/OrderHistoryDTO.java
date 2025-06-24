package com.example.TestNodo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderHistoryDTO {
    private Long orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private List<OrderItemDTO> items;

    // getters/setters, constructor
    public OrderHistoryDTO() {
    }
    public OrderHistoryDTO(Long orderId, String customerName, LocalDateTime orderDate, Double totalAmount, List<OrderItemDTO> items) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}

