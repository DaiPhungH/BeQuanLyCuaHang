package com.example.TestNodo.service;

import com.example.TestNodo.dto.CategoryDTO;
import com.example.TestNodo.dto.ProductDTO;
import com.example.TestNodo.entity.Category;
import com.example.TestNodo.entity.Product;
import com.example.TestNodo.entity.ProductCategory;
import com.example.TestNodo.repository.CategoryRepository;
import com.example.TestNodo.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductCategoryService(ProductCategoryRepository productCategoryRepository, CategoryRepository categoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void updateProductCategories(ProductDTO productDTO, Product product) {
        // Lấy danh sách ID category từ DTO
        List<Long> categoryIds = productDTO.getCategories() != null
                ? productDTO.getCategories().stream()
                .map(CategoryDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList())
                : new ArrayList<>();

        // Lấy danh sách ProductCategory hiện tại
        List<ProductCategory> currentProductCategories = productCategoryRepository.findByProduct(product);

        // Xóa các ProductCategory không còn trong DTO
        currentProductCategories.forEach(pc -> {
            if (!categoryIds.contains(pc.getCategory().getId())) {
                productCategoryRepository.delete(pc);
            }
        });

        // Thêm mới ProductCategory cho các category mới
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        List<ProductCategory> newProductCategories = categories.stream()
                .filter(category -> currentProductCategories.stream()
                        .noneMatch(pc -> pc.getCategory().getId().equals(category.getId())))
                .map(category -> {
                    ProductCategory pc = new ProductCategory();
                    pc.setProduct(product);
                    pc.setCategory(category);
                    return pc;
                })
                .collect(Collectors.toList());

        productCategoryRepository.saveAll(newProductCategories);
    }
}