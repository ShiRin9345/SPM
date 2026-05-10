package com.team.lms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateRolePermissionRequest {
    @NotBlank(message = "permissionScope is required")
    private String permissionScope;
}
