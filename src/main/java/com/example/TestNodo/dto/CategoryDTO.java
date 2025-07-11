package com.example.TestNodo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryDTO {
    private Long id;

    @NotBlank(message = "{category.name.notblank}")
    @Size(max = 100, message = "{category.name.size}")
    private String name;

    @NotBlank(message = "{category.code.notblank}")
    @Size(max = 50, message = "{category.code.size}")
    private String categoryCode;

    @Size(max = 200, message = "{category.description.size}")
    private String description;

    @Pattern(regexp = "[0-1]", message = "{category.status.pattern}")
    private String status = "1";

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String createdBy;

    private String modifiedBy;

    private List<ImageDTO> images;

    private List<String> deleteImageUuids; // hoặc List<Long> nếu bạn dùng ID


    // Getters and Setters

    public CategoryDTO() {}
    public CategoryDTO(Long id, String name, String categoryCode, String description, String status,
                       LocalDateTime createdDate, LocalDateTime modifiedDate, String createdBy,
                       String modifiedBy, List<ImageDTO> images) {
        this.id = id;
        this.name = name;
        this.categoryCode = categoryCode;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.images = images;
    }

    public List<String> getDeleteImageUuids() {
        return deleteImageUuids;
    }
    public void setDeleteImageUuids(List<String> deleteImageUuids) {
        this.deleteImageUuids = deleteImageUuids;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }
}
