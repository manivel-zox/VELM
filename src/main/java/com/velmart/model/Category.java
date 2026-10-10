package com.velmart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "category_id", length = 64, nullable = false)
    private String categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at")
    private String createdAt;

    public Category() {
    }

    public Category(String categoryId, String name, String description, String imageUrl, Boolean active, Integer displayOrder, String createdAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = active;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
