package com.example.TestNodo.service;

import com.example.TestNodo.dto.CreateOrderDTO;
import com.example.TestNodo.dto.OrderItemDTO;
import com.example.TestNodo.entity.Order;
import com.example.TestNodo.entity.OrderItem;
import com.example.TestNodo.entity.Product;
import com.example.TestNodo.repository.OrderRepository;
import com.example.TestNodo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepo;
    @Autowired private ProductRepository productRepo;

    @Transactional
    public Order createOrder(CreateOrderDTO dto) {
        Order order = new Order();

        // Gán tên khách hàng hoặc dùng mặc định nếu không nhập
        String name = dto.getCustomerName();
        if (name == null || name.trim().isEmpty()) {
            name = "Ẩn danh"; // hoặc "Ẩn anh" nếu bạn muốn giữ nguyên như bạn viết
        }
        order.setCustomerName(name);

        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (OrderItemDTO itemDTO : dto.getItems()) {
            Product product = productRepo.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException("Số lượng không đủ cho sản phẩm: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(product.getPrice());
            item.setOrder(order);
            items.add(item);

            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            total += product.getPrice() * itemDTO.getQuantity();
        }

        order.setItems(items);
        order.setTotalAmount(total);

        return orderRepo.save(order);
    }

}
