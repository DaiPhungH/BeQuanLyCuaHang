package com.example.TestNodo.repository;

import com.example.TestNodo.entity.Category;
import com.example.TestNodo.entity.CategoryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryImageRepository extends JpaRepository<CategoryImage, Long> {
    List<CategoryImage> findByUuidInAndCategory(List<String> uuids, Category category);
    List<CategoryImage> findByStatus(String status);
}