package com.team.lms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminStatusCodeUpdateRequest {
    @NotBlank(message = "displayName is required")
    private String displayName;
    private String description;
    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
