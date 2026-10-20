package com.learnloop.domain.user.dto;

import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.entity.UserStatus;

public class AdminUserStatusRequest {

    private UserStatus status;
    private UserRole role;

    public AdminUserStatusRequest() {
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
