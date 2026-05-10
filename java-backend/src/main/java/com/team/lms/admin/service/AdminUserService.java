package com.team.lms.admin.service;

import com.team.lms.admin.dto.AdminCreateUserRequest;
import com.team.lms.admin.dto.AdminUpdateRolePermissionRequest;
import com.team.lms.admin.dto.AdminUpdateSystemConfigRequest;
import com.team.lms.admin.dto.AdminUpdateUserEnabledRequest;
import com.team.lms.admin.dto.AdminUpdateUserRequest;
import com.team.lms.admin.vo.AdminRolePermissionVo;
import com.team.lms.admin.vo.AdminSystemConfigVo;
import com.team.lms.admin.vo.AdminUserVo;

import java.util.List;

public interface AdminUserService {
    AdminUserVo createUser(String authorizationHeader, AdminCreateUserRequest request);
    AdminUserVo getUser(String authorizationHeader, Long userId);
    List<AdminUserVo> listUsers(String authorizationHeader);
    AdminUserVo updateUser(String authorizationHeader, Long userId, AdminUpdateUserRequest request);
    AdminUserVo updateUserEnabled(String authorizationHeader, Long userId, AdminUpdateUserEnabledRequest request);
    List<AdminRolePermissionVo> listRolePermissions(String authorizationHeader);
    AdminRolePermissionVo updateRolePermission(String authorizationHeader, String role, AdminUpdateRolePermissionRequest request);
    List<AdminSystemConfigVo> listSystemConfigs(String authorizationHeader);
    AdminSystemConfigVo updateSystemConfig(String authorizationHeader, String configKey, AdminUpdateSystemConfigRequest request);
    List<String> getCurrentPermissions(String authorizationHeader);
}
