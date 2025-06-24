package com.example.TestNodo.dto;

import java.util.List;

public class CreateOrderDTO {
    private String customerName;
    private List<OrderItemDTO> items;

    public CreateOrderDTO() {
    }

    public CreateOrderDTO(String customerName, List<OrderItemDTO> items) {
        this.customerName = customerName;
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
