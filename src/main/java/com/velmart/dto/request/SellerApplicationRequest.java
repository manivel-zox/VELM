package com.velmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SellerApplicationRequest {
    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 150)
    private String businessName;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    @NotBlank(message = "Business phone number is required")
    @Pattern(regexp = "^[0-9+ -]{10,15}$", message = "Phone number must be valid (10-15 digits)")
    private String phone;

    public SellerApplicationRequest() {
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
}
