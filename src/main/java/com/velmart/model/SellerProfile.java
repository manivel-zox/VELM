package com.velmart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "seller_profiles", indexes = {
        @Index(name = "idx_seller_user", columnList = "user_id", unique = true)
})
public class SellerProfile {

    @Id
    @Column(name = "seller_id", length = 64, nullable = false)
    private String sellerId;

    @Column(name = "user_id", length = 64, nullable = false, unique = true)
    private String userId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", length = 32)
    private ApplicationStatus applicationStatus;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public SellerProfile() {
    }

    public SellerProfile(String sellerId, String userId, String businessName, String businessAddress, String phone, ApplicationStatus applicationStatus, String approvalNotes, String createdAt, String updatedAt) {
        this.sellerId = sellerId;
        this.userId = userId;
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.phone = phone;
        this.applicationStatus = applicationStatus;
        this.approvalNotes = approvalNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
