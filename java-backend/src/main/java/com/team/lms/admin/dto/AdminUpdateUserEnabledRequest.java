package com.team.lms.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserEnabledRequest {
    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
