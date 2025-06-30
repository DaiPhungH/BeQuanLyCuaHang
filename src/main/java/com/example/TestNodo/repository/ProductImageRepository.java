package com.example.TestNodo.repository;

import com.example.TestNodo.entity.Product;
import com.example.TestNodo.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByUuidInAndProduct(List<String> uuids, Product product);
    List<ProductImage> findByStatus(String status);
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id IN :productIds AND pi.status = '1'")
    List<ProductImage> findActiveImagesByProductIds(@Param("productIds") List<Long> productIds);

}