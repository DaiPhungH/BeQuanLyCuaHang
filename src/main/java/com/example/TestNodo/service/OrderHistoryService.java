package com.example.TestNodo.service;

import com.example.TestNodo.dto.OrderHistoryDTO;
import com.example.TestNodo.dto.OrderItemDTO;
import com.example.TestNodo.entity.Order;
import com.example.TestNodo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderHistoryService {

    @Autowired
    private OrderRepository orderRepo;

    public List<OrderHistoryDTO> getOrderHistory(String name, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.plusDays(1).atStartOfDay() : null;

        List<Order> orders = orderRepo.searchOrders(name, fromDateTime, toDateTime);

        return orders.stream().map(order -> {
            List<OrderItemDTO> items = order.getItems().stream().map(item -> new OrderItemDTO(
                    item.getProduct().getName(),
                    item.getProduct().getProductCode(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getQuantity() * item.getPrice()
            )).toList();

            return new OrderHistoryDTO(
                    order.getId(),
                    order.getCustomerName(),
                    order.getOrderDate(),
                    order.getTotalAmount(),
                    items
            );
        }).toList();
    }
}
