package com.team.lms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateSystemConfigRequest {
    @NotBlank(message = "configValue is required")
    private String configValue;

    private String description;
}
