package com.team.lms.admin.service.impl;

import com.team.lms.admin.dto.AdminCreateUserRequest;
import com.team.lms.admin.dto.AdminUpdateRolePermissionRequest;
import com.team.lms.admin.dto.AdminUpdateSystemConfigRequest;
import com.team.lms.admin.dto.AdminUpdateUserEnabledRequest;
import com.team.lms.admin.dto.AdminUpdateUserRequest;
import com.team.lms.admin.service.AdminUserService;
import com.team.lms.admin.vo.AdminRolePermissionVo;
import com.team.lms.admin.vo.AdminSystemConfigVo;
import com.team.lms.admin.vo.AdminUserVo;
import com.team.lms.common.enums.RoleType;
import com.team.lms.common.support.CurrentUserSupport;
import com.team.lms.common.support.OperationLogSupport;
import com.team.lms.common.support.PermissionScopeSupport;
import com.team.lms.entity.RolePermission;
import com.team.lms.entity.SystemConfig;
import com.team.lms.entity.User;
import com.team.lms.exception.BusinessException;
import com.team.lms.mapper.RolePermissionMapper;
import com.team.lms.mapper.SystemConfigMapper;
import com.team.lms.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final CurrentUserSupport currentUserSupport;
    private final PermissionScopeSupport permissionScopeSupport;
    private final OperationLogSupport operationLogSupport;

    @Override
    public AdminUserVo createUser(String authorizationHeader, AdminCreateUserRequest request) {
        User admin = requireAdmin(authorizationHeader);
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(400, "username already exists");
        }
        if (request.getStudentNo() != null && !request.getStudentNo().isBlank()
                && userMapper.selectByStudentNo(request.getStudentNo().trim()) != null) {
            throw new BusinessException(400, "studentNo already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword().trim());
        user.setFullName(request.getFullName().trim());
        user.setStudentNo(blankToNull(request.getStudentNo()));
        user.setPhone(blankToNull(request.getPhone()));
        user.setRole(request.getRole());
        user.setEnabled(true);
        userMapper.insert(user);
        operationLogSupport.record("USER_MANAGE", "CREATE_USER", admin.getUsername(), "Created user " + user.getUsername());

        return toVo(userMapper.selectById(user.getId()), "CREATED");
    }

    @Override
    public AdminUserVo getUser(String authorizationHeader, Long userId) {
        requireAdmin(authorizationHeader);
        User user = requireUser(userId);
        return toVo(user, Boolean.TRUE.equals(user.getEnabled()) ? "ENABLED" : "DISABLED");
    }

    @Override
    public List<AdminUserVo> listUsers(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return userMapper.selectAllActive().stream()
                .map(user -> toVo(user, Boolean.TRUE.equals(user.getEnabled()) ? "ENABLED" : "DISABLED"))
                .toList();
    }

    @Override
    public AdminUserVo updateUser(String authorizationHeader, Long userId, AdminUpdateUserRequest request) {
        User admin = requireAdmin(authorizationHeader);
        User user = requireUser(userId);

        String studentNo = blankToNull(request.getStudentNo());
        if (studentNo != null) {
            User existingStudent = userMapper.selectByStudentNo(studentNo);
            if (existingStudent != null && !existingStudent.getId().equals(userId)) {
                throw new BusinessException(400, "studentNo already exists");
            }
        }

        user.setPassword(request.getPassword().trim());
        user.setFullName(request.getFullName().trim());
        user.setStudentNo(studentNo);
        user.setPhone(blankToNull(request.getPhone()));
        user.setRole(request.getRole());
        userMapper.update(user);
        operationLogSupport.record("USER_MANAGE", "UPDATE_USER", admin.getUsername(), "Updated user " + user.getUsername());
        return toVo(userMapper.selectById(userId), "UPDATED");
    }

    @Override
    public AdminUserVo updateUserEnabled(String authorizationHeader, Long userId, AdminUpdateUserEnabledRequest request) {
        User admin = requireAdmin(authorizationHeader);
        User target = requireUser(userId);
        userMapper.updateEnabledById(userId, request.getEnabled());
        operationLogSupport.record("USER_MANAGE", "UPDATE_USER_STATUS", admin.getUsername(), "Set user " + target.getUsername() + " enabled=" + request.getEnabled());
        return toVo(userMapper.selectById(userId), Boolean.TRUE.equals(request.getEnabled()) ? "ENABLED" : "DISABLED");
    }

    @Override
    public List<AdminRolePermissionVo> listRolePermissions(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        permissionScopeSupport.ensureRolePermissionsSeeded();
        return rolePermissionMapper.selectAll().stream()
                .map(item -> AdminRolePermissionVo.builder()
                        .role(item.getRole().name())
                        .permissionScope(item.getPermissionScope())
                        .build())
                .toList();
    }

    @Override
    public AdminRolePermissionVo updateRolePermission(String authorizationHeader, String role, AdminUpdateRolePermissionRequest request) {
        User admin = requireAdmin(authorizationHeader);
        RoleType roleType = parseRole(role);
        permissionScopeSupport.ensureRolePermissionsSeeded();

        RolePermission rolePermission = rolePermissionMapper.selectByRole(roleType);
        if (rolePermission == null) {
            rolePermission = new RolePermission();
            rolePermission.setRole(roleType);
            rolePermission.setPermissionScope(request.getPermissionScope().trim());
            rolePermissionMapper.insert(rolePermission);
        } else {
            rolePermission.setPermissionScope(request.getPermissionScope().trim());
            rolePermissionMapper.update(rolePermission);
        }

        RolePermission saved = rolePermissionMapper.selectByRole(roleType);
        operationLogSupport.record("SYSTEM_RULE", "ROLE_PERMISSION_UPDATE", admin.getUsername(), "Updated permission scope for " + saved.getRole().name());
        return AdminRolePermissionVo.builder()
                .role(saved.getRole().name())
                .permissionScope(saved.getPermissionScope())
                .build();
    }

    @Override
    public List<AdminSystemConfigVo> listSystemConfigs(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        ensureSystemConfigsSeeded();
        return systemConfigMapper.selectAll().stream()
                .map(item -> AdminSystemConfigVo.builder()
                        .configKey(item.getConfigKey())
                        .configValue(item.getConfigValue())
                        .description(item.getDescription())
                        .build())
                .toList();
    }

    @Override
    public AdminSystemConfigVo updateSystemConfig(String authorizationHeader, String configKey, AdminUpdateSystemConfigRequest request) {
        User admin = requireAdmin(authorizationHeader);
        ensureSystemConfigsSeeded();

        String normalizedKey = configKey.trim().toUpperCase(Locale.ROOT);
        SystemConfig config = systemConfigMapper.selectByKey(normalizedKey);
        if (config == null) {
            throw new BusinessException(404, "system config not found");
        }

        config.setConfigValue(request.getConfigValue().trim());
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription().trim());
        }
        systemConfigMapper.update(config);

        SystemConfig saved = systemConfigMapper.selectByKey(normalizedKey);
        operationLogSupport.record("SYSTEM_RULE", "BUSINESS_PARAMETER_UPDATE", admin.getUsername(), "Updated system parameter " + saved.getConfigKey());
        return AdminSystemConfigVo.builder()
                .configKey(saved.getConfigKey())
                .configValue(saved.getConfigValue())
                .description(saved.getDescription())
                .build();
    }

    @Override
    public List<String> getCurrentPermissions(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return permissionScopeSupport.getCurrentPermissions(authorizationHeader);
    }

    private User requireAdmin(String authorizationHeader) {
        User user = currentUserSupport.requireUser(authorizationHeader);
        if (user.getRole() != RoleType.ADMIN) {
            throw new BusinessException(403, "current user is not an admin");
        }
        return user;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        return user;
    }

    private RoleType parseRole(String role) {
        try {
            return RoleType.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(400, "role must be READER/LIBRARIAN/ADMIN");
        }
    }

    private void ensureSystemConfigsSeeded() {
        seedSystemConfig("BORROW_PERIOD_DAYS", "30", "Default borrowing period in days");
        seedSystemConfig("BORROW_LIMIT", "5", "Maximum number of borrowed books");
        seedSystemConfig("OVERDUE_FINE_PER_DAY", "1.00", "Overdue fine charged per day");
    }

    private void seedSystemConfig(String key, String value, String description) {
        if (systemConfigMapper.selectByKey(key) != null) {
            return;
        }
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        systemConfigMapper.insert(config);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AdminUserVo toVo(User user, String status) {
        return AdminUserVo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .studentNo(user.getStudentNo())
                .phone(user.getPhone())
                .role(user.getRole() == null ? null : user.getRole().name())
                .enabled(user.getEnabled())
                .status(status)
                .build();
    }
}
