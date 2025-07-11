package com.example.TestNodo.service;

import com.example.TestNodo.dto.CategoryDTO;
import com.example.TestNodo.dto.ImageDTO;
import com.example.TestNodo.entity.Category;
import com.example.TestNodo.entity.CategoryImage;
import com.example.TestNodo.mapper.CategoryMapper;
import com.example.TestNodo.repository.CategoryImageRepository;
import com.example.TestNodo.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryImageRepository categoryImageRepository;
    private final CategoryMapper categoryMapper;
    private final MessageSource messageSource;
    private final ImageService imageService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CategoryImageRepository categoryImageRepository,
                           CategoryMapper categoryMapper, MessageSource messageSource, ImageService imageService) {
        this.categoryRepository = categoryRepository;
        this.categoryImageRepository = categoryImageRepository;
        this.categoryMapper = categoryMapper;
        this.messageSource = messageSource;
        this.imageService = imageService;
    }

    private List<ImageDTO> toImageDTOs(List<CategoryImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(img -> "1".equals(img.getStatus()))
                .map(img -> {
                    ImageDTO imgDTO = new ImageDTO();
                    imgDTO.setName(img.getName());
                    imgDTO.setUrl(img.getUrl());
                    imgDTO.setUuid(img.getUuid());
                    return imgDTO;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(@Valid CategoryDTO categoryDTO, List<MultipartFile> images) {
        if (categoryRepository.existsByCategoryCode(categoryDTO.getCategoryCode())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("category.code.exists", null, LocaleContextHolder.getLocale())
            );
        }
        Category category = categoryMapper.toEntity(categoryDTO);
        category.setCreatedDate(LocalDateTime.now());
        category.setCreatedBy("admin");
        category.setStatus("1");

        // Lưu category trước để sinh ID
        try {
            category = categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException(
                    messageSource.getMessage("category.save.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }

        // Sử dụng ImageService để lưu hình ảnh vào thư mục cục bộ và tạo URL
        List<CategoryImage> categoryImages = imageService.uploadCategoryImages(images, category);
        category.setImages(categoryImages);

        // Lưu lại category để cập nhật quan hệ
        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException(
                    messageSource.getMessage("category.save.with.images.failed", null, LocaleContextHolder.getLocale()) + ": " + e.getMessage(), e
            );
        }

        CategoryDTO result = categoryMapper.toDTO(category);
        result.setImages(toImageDTOs(categoryImages));
        return result;
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, @Valid CategoryDTO categoryDTO, List<MultipartFile> images, List<String> deletedImageUuids) {
        Category category = categoryRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSource.getMessage("category.not.found", null, LocaleContextHolder.getLocale())
                ));

        // Kiểm tra mã danh mục không bị trùng nếu thay đổi
        if (!category.getCategoryCode().equals(categoryDTO.getCategoryCode()) &&
                categoryRepository.existsByCategoryCode(categoryDTO.getCategoryCode())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("category.code.exists", null, LocaleContextHolder.getLocale())
            );
        }

        // Cập nhật thông tin cơ bản
        category.setName(categoryDTO.getName());
        category.setCategoryCode(categoryDTO.getCategoryCode());
        category.setDescription(categoryDTO.getDescription());
        category.setStatus(categoryDTO.getStatus());
        category.setModifiedDate(LocalDateTime.now());
        category.setModifiedBy("admin");

        // ✅ Gọi hàm cập nhật ảnh: có xoá ảnh cũ (theo UUID) và thêm ảnh mới
        imageService.updateCategoryImages(images, deletedImageUuids, category);

        // Lưu lại category
        categoryRepository.save(category);

        // Convert sang DTO và gắn danh sách ảnh mới
        CategoryDTO result = categoryMapper.toDTO(category);
        result.setImages(toImageDTOs(category.getImages()));
        return result;
    }



    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSource.getMessage("category.not.found", null, LocaleContextHolder.getLocale())
                ));
        category.setStatus("0");
        category.setModifiedDate(LocalDateTime.now());
        category.setModifiedBy("admin");
        categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDTO> searchCategories(String name, String categoryCode,
                                              LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.search(name, categoryCode, createdFrom, createdTo, pageable);
        return categoryPage.map(category -> {
            CategoryDTO dto = categoryMapper.toDTO(category);
            dto.setImages(toImageDTOs(category.getImages())); // không còn N+1
            return dto;
        });
    }


    @Transactional(readOnly = true)
    public byte[] exportCategoriesToExcel(String name, String categoryCode, LocalDateTime createdFrom, LocalDateTime createdTo, String lang) throws Exception {
        Locale locale = (lang != null && (lang.equals("en") || lang.equals("vi"))) ? new Locale(lang) : LocaleContextHolder.getLocale();

        List<Category> categories = categoryRepository.searchAll(name, categoryCode, createdFrom, createdTo);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Categories");

        // Header
        Row headerRow = sheet.createRow(0);
        String[] columns = {
                messageSource.getMessage("excel.header.id", null, locale),
                messageSource.getMessage("excel.header.name", null, locale),
                messageSource.getMessage("excel.header.code", null, locale),
                messageSource.getMessage("excel.header.description", null, locale),
                messageSource.getMessage("excel.header.createdDate", null, locale),
                messageSource.getMessage("excel.header.modifiedDate", null, locale),
                messageSource.getMessage("excel.header.createdBy", null, locale),
                messageSource.getMessage("excel.header.modifiedBy", null, locale)
        };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Data
        int rowNum = 1;
        for (Category category : categories) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(category.getId());
            row.createCell(1).setCellValue(category.getName());
            row.createCell(2).setCellValue(category.getCategoryCode());
            row.createCell(3).setCellValue(category.getDescription());
            row.createCell(4).setCellValue(category.getCreatedDate().toString());
            row.createCell(5).setCellValue(category.getModifiedDate() != null ? category.getModifiedDate().toString() : "");
            row.createCell(6).setCellValue(category.getCreatedBy());
            row.createCell(7).setCellValue(category.getModifiedBy() != null ? category.getModifiedBy() : "");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException(
                    messageSource.getMessage("excel.export.failed", null, locale) + ": " + e.getMessage(), e
            );
        } finally {
            workbook.close();
        }

        return out.toByteArray();
    }


    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSource.getMessage("category.not.found", null, LocaleContextHolder.getLocale())
                ));

        CategoryDTO dto = categoryMapper.toDTO(category);
        dto.setImages(toImageDTOs(category.getImages()));
        return dto;
    }

}