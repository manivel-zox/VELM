package com.velmart.service;

import com.velmart.dto.request.CategoryRequest;
import com.velmart.exception.BadRequestException;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.model.Category;
import com.velmart.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public CategoryService(CategoryRepository categoryRepository, AuditLogService auditLogService) {
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findAllActive();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    public Category createCategory(CategoryRequest request, String adminId, String adminEmail, String clientIp) {
        String categoryId = request.getCategoryId();
        if (categoryId == null || categoryId.isBlank()) {
            categoryId = request.getName().toLowerCase().replaceAll("[^a-z0-9]", "-");
        }

        if (categoryRepository.findById(categoryId).isPresent()) {
            categoryId = categoryId + "-" + UUID.randomUUID().toString().substring(0, 4);
        }

        Category category = new Category(
                categoryId,
                request.getName().trim(),
                request.getDescription(),
                request.getImageUrl(),
                request.getActive() != null ? request.getActive() : true,
                request.getDisplayOrder() != null ? request.getDisplayOrder() : 0,
                Instant.now().toString()
        );

        categoryRepository.save(category);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN", "CREATE_CATEGORY",
                "CATEGORY", categoryId, "Created category: " + category.getName(), clientIp);

        return category;
    }

    public Category updateCategory(String categoryId, CategoryRequest request, String adminId, String adminEmail, String clientIp) {
        Category category = getCategoryById(categoryId);

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        categoryRepository.save(category);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN", "UPDATE_CATEGORY",
                "CATEGORY", categoryId, "Updated category: " + category.getName(), clientIp);

        return category;
    }

    public void deleteCategory(String categoryId, String adminId, String adminEmail, String clientIp) {
        Category category = getCategoryById(categoryId);
        categoryRepository.delete(categoryId);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN", "DELETE_CATEGORY",
                "CATEGORY", categoryId, "Deleted category: " + category.getName(), clientIp);
    }
}
