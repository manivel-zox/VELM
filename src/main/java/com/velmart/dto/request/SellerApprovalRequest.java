package com.velmart.dto.request;

import com.velmart.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class SellerApprovalRequest {
    @NotNull(message = "Approval status is required (APPROVED or REJECTED)")
    private ApplicationStatus status;

    private String notes;

    public SellerApprovalRequest() {
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
