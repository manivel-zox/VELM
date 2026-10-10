package com.velmart.dto.request;

import com.velmart.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public class UserStatusRequest {
    @NotNull(message = "Account status is required (ACTIVE or SUSPENDED)")
    private AccountStatus status;

    public UserStatusRequest() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
