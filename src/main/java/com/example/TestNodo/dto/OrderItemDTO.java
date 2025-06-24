package com.example.TestNodo.dto;

public class OrderItemDTO {
    private Long productId;
    private String productName;
    private String productCode;
    private int quantity;
    private double price;
    private double total;

    // getters/setters, constructor
    public OrderItemDTO() {
    }

    public OrderItemDTO(String productName, String productCode, int quantity, double price, double total) {
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.price = price;
        this.total = total;

    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }


}
