package com.team.lms.admin.dto;

import com.team.lms.common.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    @NotBlank(message = "fullName is required")
    private String fullName;

    private String studentNo;

    private String phone;

    @NotNull(message = "role is required")
    private RoleType role;
}
