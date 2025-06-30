package com.example.TestNodo.repository;

import com.example.TestNodo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductCode(String productCode);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.productCategories pc " +
            "WHERE p.status = '1' " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:productCode IS NULL OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :productCode, '%'))) " +
            "AND (:createdFrom IS NULL OR p.createdDate >= :createdFrom) " +
            "AND (:createdTo IS NULL OR p.createdDate <= :createdTo) " +
            "AND (:categoryId IS NULL OR pc.category.id = :categoryId AND pc.status = '1')")
    Page<Product> search(String name, String productCode, LocalDateTime createdFrom, LocalDateTime createdTo, Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.productCategories pc
            LEFT JOIN FETCH pc.category c
            WHERE p.status = '1'
              AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:productCode IS NULL OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :productCode, '%')))
              AND (:createdFrom IS NULL OR p.createdDate >= :createdFrom)
              AND (:createdTo IS NULL OR p.createdDate <= :createdTo)
              AND (:categoryId IS NULL OR EXISTS (
                    SELECT 1 FROM ProductCategory pc2
                    WHERE pc2.product = p AND pc2.category.id = :categoryId AND pc2.status = '1'
              ))
            """)
    List<Product> searchAllForExport(@Param("name") String name,
                                     @Param("productCode") String productCode,
                                     @Param("createdFrom") LocalDateTime createdFrom,
                                     @Param("createdTo") LocalDateTime createdTo,
                                     @Param("categoryId") Long categoryId);

}