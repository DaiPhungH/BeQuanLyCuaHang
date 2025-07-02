package com.example.TestNodo.service;

import com.example.TestNodo.dto.ImageDTO;
import com.example.TestNodo.dto.ProductDTO;
import com.example.TestNodo.entity.Category;
import com.example.TestNodo.entity.Product;
import com.example.TestNodo.entity.ProductCategory;
import com.example.TestNodo.entity.ProductImage;
import com.example.TestNodo.mapper.CategoryMapper;
import com.example.TestNodo.mapper.ProductMapper;
import com.example.TestNodo.repository.CategoryRepository;
import com.example.TestNodo.repository.ProductCategoryRepository;
import com.example.TestNodo.repository.ProductImageRepository;
import com.example.TestNodo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final ImageService imageService;
    private final CategoryMapper categoryMapper;
    private final MessageSource messageSource;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          ProductCategoryRepository productCategoryRepository, ProductImageRepository productImageRepository,
                          ProductMapper productMapper, CategoryMapper categoryMapper, MessageSource messageSource, ImageService imageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productImageRepository = productImageRepository;
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.messageSource = messageSource;
        this.imageService = imageService;
    }

    @Transactional
    public ProductDTO createProduct(@Valid ProductDTO productDTO, List<MultipartFile> images, List<Long> categoryIds) {
        logger.info("Creating product with code: {}", productDTO.getProductCode());
        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            logger.error("Product code {} already exists", productDTO.getProductCode());
            throw new IllegalArgumentException(messageSource.getMessage("product.code.exists", null, LocaleContextHolder.getLocale()));
        }
        Product product = productMapper.toEntity(productDTO);
        product.setCreatedDate(LocalDateTime.now());
        product.setCreatedBy("admin");
        product.setStatus("1");

        try {
            product = productRepository.save(product);
            logger.info("Product saved with ID: {}", product.getId());
        } catch (Exception e) {
            logger.error("Failed to save product: {}", e.getMessage());
            throw new RuntimeException(
                    messageSource.getMessage("product.save.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }

        List<ProductImage> productImages = imageService.uploadProductImages(images, product);
        product.setImages(productImages);

        product.setProductCategories(createProductCategories(product, categoryIds));

        try {
            productRepository.save(product);
            logger.info("Product with images and categories saved successfully for ID: {}", product.getId());
        } catch (Exception e) {
            logger.error("Failed to save product with images/categories: {}", e.getMessage());
            throw new RuntimeException(
                    messageSource.getMessage("product.save.with.images.categories.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }

        return toProductDTO(product);
    }
    @Transactional
    public List<ProductCategory> createProductCategories(Product product, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return categoryIds.stream().map(categoryId -> {
            Category category = categoryRepository.findByIdWithImages(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("category.not.found", null, LocaleContextHolder.getLocale())));
            ProductCategory pc = new ProductCategory();
            pc.setProduct(product);
            pc.setCategory(category);
            pc.setCreatedDate(LocalDateTime.now());
            pc.setStatus("1");
            return pc;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO updateProduct(Long id, @Valid ProductDTO productDTO, List<MultipartFile> images,
                                    List<String> deletedImageUuids, List<Long> categoryIds) {
        logger.info("Updating product with ID: {}", id);

        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSource.getMessage("product.not.found", null, LocaleContextHolder.getLocale())
                ));

        if (!product.getProductCode().equals(productDTO.getProductCode()) &&
                productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("product.code.exists", null, LocaleContextHolder.getLocale())
            );
        }

        // Cập nhật thông tin cơ bản
        product.setName(productDTO.getName());
        product.setProductCode(productDTO.getProductCode());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setStatus(productDTO.getStatus());
        product.setModifiedDate(LocalDateTime.now());
        product.setModifiedBy("admin");

        // ✅ Cập nhật ảnh: thêm mới & đánh dấu xóa ảnh cũ
        imageService.updateProductImages(images, deletedImageUuids, product);

        // ✅ Cập nhật danh mục sản phẩm nếu có
        if (categoryIds != null) {
            updateProductCategories(product, categoryIds);
            logger.info("Updated categories for product ID: {}", id);
        }

        try {
            productRepository.save(product);
            logger.info("Product updated successfully for ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to update product: {}", e.getMessage());
            throw new RuntimeException(
                    messageSource.getMessage("product.update.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }

        return toProductDTO(product);
    }

    @Transactional
    public void updateProductCategories(Product product, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        product.getProductCategories().forEach(pc -> pc.setStatus("0"));
        categoryIds.forEach(categoryId -> {
            Category category = categoryRepository.findByIdWithImages(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("category.not.found", null, LocaleContextHolder.getLocale())));
            ProductCategory existing = product.getProductCategories().stream()
                    .filter(pc -> pc.getStatus().equals("0") && pc.getCategory().getId().equals(categoryId))
                    .findFirst().orElse(null);
            if (existing != null) {
                existing.setStatus("1");
                existing.setModifiedDate(LocalDateTime.now());
            } else {
                ProductCategory pc = new ProductCategory();
                pc.setProduct(product);
                pc.setCategory(category);
                pc.setCreatedDate(LocalDateTime.now());
                pc.setStatus("1");
                product.getProductCategories().add(pc);
            }
        });
    }

    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("product.not.found", null, LocaleContextHolder.getLocale())));
        product.setStatus("0");
        product.setModifiedDate(LocalDateTime.now());
        product.setModifiedBy("admin");
        try {
            productRepository.save(product);
            logger.info("Product deleted (soft) successfully for ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete product: {}", e.getMessage());
            throw new RuntimeException(
                    messageSource.getMessage("product.delete.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String name, String productCode, LocalDateTime createdFrom,
                                           LocalDateTime createdTo, Long categoryId, Pageable pageable) {
        logger.info("Searching products with name: {}, code: {}, categoryId: {}", name, productCode, categoryId);

        // 1. Truy vấn sản phẩm theo điều kiện
        Page<Product> productPage = productRepository.search(name, productCode, createdFrom, createdTo, categoryId, pageable);

        // 2. Lấy danh sách ID sản phẩm
        List<Long> productIds = productPage.getContent().stream()
                .map(Product::getId)
                .toList();

        // 3. Lấy ảnh theo productId
        List<ProductImage> allImages = productImageRepository.findActiveImagesByProductIds(productIds);

        // 4. Nhóm ảnh theo productId
        Map<Long, List<ProductImage>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getProduct().getId()));

        // 5. Gán ảnh vào DTO
        return productPage.map(product -> {
            ProductDTO dto = toProductDTO(product);
            dto.setImages(toImageDTOs(imageMap.getOrDefault(product.getId(), List.of())));
            return dto;
        });
    }


    private List<ImageDTO> toImageDTOs(List<ProductImage> images) {
        if (images == null) return List.of();
        return images.stream()
                .filter(img -> "1".equals(img.getStatus()))
                .map(img -> {
                    ImageDTO dto = new ImageDTO();
                    dto.setName(img.getName());
                    dto.setUrl(img.getUrl());
                    dto.setUuid(img.getUuid());
                    return dto;
                }).collect(Collectors.toList());
    }

    private ProductDTO toProductDTO(Product product) {
        ProductDTO dto = productMapper.toDTO(product);
        dto.setImages(toImageDTOs(product.getImages()));
        dto.setCategories(product.getProductCategories().stream()
                .filter(pc -> pc.getStatus() != null && pc.getStatus().equals("1"))
                .map(pc -> {
                    Category category = categoryRepository.findByIdWithImages(pc.getCategory().getId())
                            .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("category.not.found", null, LocaleContextHolder.getLocale())));
                    return categoryMapper.toDTOWithImages(category);
                }).collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public byte[] exportProductsToExcel(String name, String productCode, LocalDateTime createdFrom, LocalDateTime createdTo,
                                        Long categoryId, String lang) throws IOException {
        logger.info("Exporting products to Excel with name: {}, code: {}, categoryId: {}", name, productCode, categoryId);
        Locale locale = (lang != null && (lang.equals("en") || lang.equals("vi"))) ? new Locale(lang) : LocaleContextHolder.getLocale();

        // ❌ Bỏ dùng PageRequest, dùng List thay thế
        List<Product> products = productRepository.searchAllForExport(name, productCode, createdFrom, createdTo, categoryId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        Row headerRow = sheet.createRow(0);
        String[] columns = {
                messageSource.getMessage("excel.header.id", null, locale),
                messageSource.getMessage("excel.header.name", null, locale),
                messageSource.getMessage("excel.header.code", null, locale),
                messageSource.getMessage("excel.header.price", null, locale),
                messageSource.getMessage("excel.header.quantity", null, locale),
                messageSource.getMessage("excel.header.createdDate", null, locale),
                messageSource.getMessage("excel.header.modifiedDate", null, locale),
                messageSource.getMessage("excel.header.categories", null, locale)
        };
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getProductCode());
            row.createCell(3).setCellValue(product.getPrice());
            row.createCell(4).setCellValue(product.getQuantity());
            row.createCell(5).setCellValue(product.getCreatedDate().toString());
            row.createCell(6).setCellValue(product.getModifiedDate() != null ? product.getModifiedDate().toString() : "");
            String categories = product.getProductCategories().stream()
                    .filter(pc -> "1".equals(pc.getStatus()))
                    .map(pc -> pc.getCategory().getName())
                    .collect(Collectors.joining(", "));
            row.createCell(7).setCellValue(categories);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            logger.info("Exported products to Excel successfully");
            return out.toByteArray();
        } finally {
            workbook.close();
        }
    }


    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        logger.info("Fetching product with ID: {}", id);
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSource.getMessage("product.not.found", null, LocaleContextHolder.getLocale())
                ));

        return toProductDTO(product);
    }

}