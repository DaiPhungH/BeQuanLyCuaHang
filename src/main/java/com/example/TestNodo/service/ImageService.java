package com.example.TestNodo.service;

import com.example.TestNodo.dto.CategoryDTO;
import com.example.TestNodo.dto.ProductDTO;
import com.example.TestNodo.entity.Category;
import com.example.TestNodo.entity.CategoryImage;
import com.example.TestNodo.entity.Product;
import com.example.TestNodo.entity.ProductImage;
import com.example.TestNodo.repository.CategoryImageRepository;
import com.example.TestNodo.repository.ProductImageRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ProductImageRepository productImageRepository;
    private final CategoryImageRepository categoryImageRepository;
    private final MessageSource messageSource;

    private static final String PRODUCT_UPLOAD_DIR = "src/main/resources/static/images/products/";
    private static final String CATEGORY_UPLOAD_DIR = "src/main/resources/static/images/categories/";
    private static final String PRODUCT_URL_PREFIX = "/images/products/";
    private static final String CATEGORY_URL_PREFIX = "/images/categories/";

    @Autowired
    public ImageService(ProductImageRepository productImageRepository, CategoryImageRepository categoryImageRepository, MessageSource messageSource) {
        this.productImageRepository = productImageRepository;
        this.categoryImageRepository = categoryImageRepository;
        this.messageSource = messageSource;
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(PRODUCT_UPLOAD_DIR));
            Files.createDirectories(Paths.get(CATEGORY_UPLOAD_DIR));
        } catch (IOException e) {
            logger.error("Failed to create image directories: {}", e.getMessage());
            throw new RuntimeException(
                    messageSource.getMessage("image.dir.create.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }
    }

    @Transactional
    public List<ProductImage> uploadProductImages(List<MultipartFile> files, Product product) {
        if (files == null || files.isEmpty()) {
            logger.info("No product images to upload for product ID: {}", product.getId());
            return List.of();
        }
        List<ProductImage> productImages = files.stream().map(file -> {
            try {
                validateImageFile(file);
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(PRODUCT_UPLOAD_DIR + fileName);
                Files.write(filePath, file.getBytes());

                ProductImage image = new ProductImage();
                image.setName(file.getOriginalFilename());
                image.setUrl(PRODUCT_URL_PREFIX + fileName);
                image.setUuid(UUID.randomUUID().toString());
                image.setStatus("1");
                image.setProduct(product);
                return image;
            } catch (IOException e) {
                logger.error("Failed to save product image {}: {}", file.getOriginalFilename(), e.getMessage());
                throw new RuntimeException(
                        messageSource.getMessage("image.product.save.failed", null, LocaleContextHolder.getLocale()) + ": " + file.getOriginalFilename(), e
                );
            }
        }).collect(Collectors.toList());
        logger.info("Successfully uploaded {} product images for product ID: {}", productImages.size(), product.getId());
        return productImageRepository.saveAll(productImages);
    }

    @Transactional
    public List<CategoryImage> uploadCategoryImages(List<MultipartFile> files, Category category) {
        if (files == null || files.isEmpty()) {
            logger.info("No category images to upload for category ID: {}", category.getId());
            return List.of();
        }
        List<CategoryImage> categoryImages = files.stream().map(file -> {
            try {
                validateImageFile(file);
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(CATEGORY_UPLOAD_DIR + fileName);
                Files.write(filePath, file.getBytes());

                CategoryImage image = new CategoryImage();
                image.setName(file.getOriginalFilename());
                image.setUrl(CATEGORY_URL_PREFIX + fileName);
                image.setUuid(UUID.randomUUID().toString());
                image.setStatus("1");
                image.setCategory(category);
                return image;
            } catch (IOException e) {
                logger.error("Failed to save category image {}: {}", file.getOriginalFilename(), e.getMessage());
                throw new RuntimeException(
                        messageSource.getMessage("image.category.save.failed", null, LocaleContextHolder.getLocale()) + ": " + file.getOriginalFilename(), e
                );
            }
        }).collect(Collectors.toList());
        logger.info("Successfully uploaded {} category images for category ID: {}", categoryImages.size(), category.getId());
        return categoryImageRepository.saveAll(categoryImages);
    }

    @Transactional
    public void updateProductImages(List<MultipartFile> files, List<String> deletedImageUuids, Product product) {
        logger.info("Updating images for product ID: {}", product.getId());

        List<ProductImage> currentImages = product.getImages();

        // 1. Xoá ảnh theo UUID nếu có
        if (deletedImageUuids != null && !deletedImageUuids.isEmpty()) {
            List<ProductImage> toDelete = currentImages.stream()
                    .filter(img -> deletedImageUuids.contains(img.getUuid()))
                    .collect(Collectors.toList());

            for (ProductImage img : toDelete) {
                img.setStatus("0"); // Soft delete
                logger.info("Marked product image {} as deleted", img.getUuid());
            }
        }

        // 2. Thêm ảnh mới nếu có
        if (files != null && !files.isEmpty()) {
            List<ProductImage> newImages = uploadProductImages(files, product);
            currentImages.addAll(newImages);
            logger.info("Added {} new images for product ID: {}", newImages.size(), product.getId());
        }

        logger.info("Finished updating product images for product ID: {}", product.getId());
    }

    @Transactional
    public void updateCategoryImages(List<MultipartFile> files, List<String> deletedImageUuids, Category category) {
        logger.info("Updating images for category ID: {}", category.getId());

        List<CategoryImage> currentImages = category.getImages();

        // 1. Xoá ảnh theo UUID nếu có
        if (deletedImageUuids != null && !deletedImageUuids.isEmpty()) {
            List<CategoryImage> toDelete = currentImages.stream()
                    .filter(img -> deletedImageUuids.contains(img.getUuid()))
                    .collect(Collectors.toList());

            for (CategoryImage img : toDelete) {
                img.setStatus("0"); // Soft delete
                logger.info("Marked category image {} as deleted", img.getUuid());
            }
        }

        // 2. Thêm ảnh mới nếu có
        if (files != null && !files.isEmpty()) {
            List<CategoryImage> newImages = uploadCategoryImages(files, category);
            currentImages.addAll(newImages);
            logger.info("Added {} new images for category ID: {}", newImages.size(), category.getId());
        }

        logger.info("Finished updating category images for category ID: {}", category.getId());
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("image.file.empty", null, LocaleContextHolder.getLocale())
            );
        }
        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png").contains(contentType)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("image.file.invalid", null, LocaleContextHolder.getLocale())
            );
        }
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new IllegalArgumentException(
                    messageSource.getMessage("image.file.too.large", null, LocaleContextHolder.getLocale())
            );
        }
    }
}
