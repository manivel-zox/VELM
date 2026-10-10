package com.velmart.dto.response;

import com.velmart.model.ApplicationStatus;

public class SellerProfileDto {
    private String sellerId;
    private String userId;
    private String businessName;
    private String businessAddress;
    private String phone;
    private ApplicationStatus applicationStatus;
    private String approvalNotes;
    private String createdAt;

    public SellerProfileDto() {
    }

    public SellerProfileDto(String sellerId, String userId, String businessName, String businessAddress, String phone, ApplicationStatus applicationStatus, String approvalNotes, String createdAt) {
        this.sellerId = sellerId;
        this.userId = userId;
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.phone = phone;
        this.applicationStatus = applicationStatus;
        this.approvalNotes = approvalNotes;
        this.createdAt = createdAt;
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
}
