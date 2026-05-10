package com.team.lms.admin.controller;

import com.team.lms.admin.dto.AdminCreateUserRequest;
import com.team.lms.admin.dto.AdminUpdateRolePermissionRequest;
import com.team.lms.admin.dto.AdminUpdateSystemConfigRequest;
import com.team.lms.admin.dto.AdminUpdateUserEnabledRequest;
import com.team.lms.admin.dto.AdminUpdateUserRequest;
import com.team.lms.admin.service.AdminUserService;
import com.team.lms.admin.vo.AdminRolePermissionVo;
import com.team.lms.admin.vo.AdminSystemConfigVo;
import com.team.lms.admin.vo.AdminUserVo;
import com.team.lms.common.api.ApiResponse;
import com.team.lms.common.api.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController extends BaseController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ApiResponse<AdminUserVo> createUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody AdminCreateUserRequest request
    ) {
        return success(adminUserService.createUser(authorizationHeader, request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminUserVo> getUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId
    ) {
        return success(adminUserService.getUser(authorizationHeader, userId));
    }

    @GetMapping
    public ApiResponse<List<AdminUserVo>> listUsers(@RequestHeader("Authorization") String authorizationHeader) {
        return success(adminUserService.listUsers(authorizationHeader));
    }

    @PutMapping("/{userId}")
    public ApiResponse<AdminUserVo> updateUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        return success(adminUserService.updateUser(authorizationHeader, userId, request));
    }

    @PatchMapping("/{userId}/enabled")
    public ApiResponse<AdminUserVo> updateUserEnabled(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserEnabledRequest request
    ) {
        return success(adminUserService.updateUserEnabled(authorizationHeader, userId, request));
    }

    @GetMapping("/roles/permissions")
    public ApiResponse<List<AdminRolePermissionVo>> listRolePermissions(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return success(adminUserService.listRolePermissions(authorizationHeader));
    }

    @GetMapping("/me/permissions")
    public ApiResponse<List<String>> getCurrentPermissions(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return success(adminUserService.getCurrentPermissions(authorizationHeader));
    }

    @PutMapping("/roles/{role}/permissions")
    public ApiResponse<AdminRolePermissionVo> updateRolePermission(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String role,
            @Valid @RequestBody AdminUpdateRolePermissionRequest request
    ) {
        return success(adminUserService.updateRolePermission(authorizationHeader, role, request));
    }

    @GetMapping("/system-parameters")
    public ApiResponse<List<AdminSystemConfigVo>> listSystemConfigs(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return success(adminUserService.listSystemConfigs(authorizationHeader));
    }

    @PutMapping("/system-parameters/{configKey}")
    public ApiResponse<AdminSystemConfigVo> updateSystemConfig(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String configKey,
            @Valid @RequestBody AdminUpdateSystemConfigRequest request
    ) {
        return success(adminUserService.updateSystemConfig(authorizationHeader, configKey, request));
    }
}
