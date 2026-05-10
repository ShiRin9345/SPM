package com.team.lms.common.support;

import com.team.lms.common.enums.RoleType;
import com.team.lms.entity.RolePermission;
import com.team.lms.entity.User;
import com.team.lms.exception.BusinessException;
import com.team.lms.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionScopeSupport {

    private final CurrentUserSupport currentUserSupport;
    private final RolePermissionMapper rolePermissionMapper;

    public void ensureRolePermissionsSeeded() {
        for (RoleType role : RoleType.values()) {
            if (rolePermissionMapper.selectByRole(role) == null) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermissionScope(defaultPermissionScope(role));
                rolePermissionMapper.insert(rolePermission);
            }
        }
    }

    public List<String> getCurrentPermissions(String authorizationHeader) {
        User user = currentUserSupport.requireUser(authorizationHeader);
        return getPermissionsByRole(user.getRole());
    }

    public List<String> getPermissionsByRole(RoleType role) {
        ensureRolePermissionsSeeded();
        RolePermission permission = rolePermissionMapper.selectByRole(role);
        return splitPermissionScope(permission == null ? null : permission.getPermissionScope());
    }

    public void requirePermission(String authorizationHeader, RoleType requiredRole, String permissionCode) {
        requireAnyPermission(authorizationHeader, requiredRole, List.of(permissionCode));
    }

    public void requireAnyPermission(String authorizationHeader, RoleType requiredRole, List<String> permissionCodes) {
        User user = currentUserSupport.requireUser(authorizationHeader);
        if (user.getRole() != requiredRole) {
            throw new BusinessException(403, "current user is not a " + requiredRole.name().toLowerCase());
        }

        List<String> permissions = getPermissionsByRole(requiredRole);
        if (permissionCodes.stream().noneMatch(permissions::contains)) {
            throw new BusinessException(403, "current user does not have the required permission");
        }
    }

    private String defaultPermissionScope(RoleType role) {
        return switch (role) {
            case READER -> "BOOK_SEARCH,BOOK_VIEW,BORROW_REQUEST,RETURN_REQUEST,RESERVATION";
            case LIBRARIAN -> "BOOK_MANAGE,INVENTORY_MANAGE,REQUEST_PROCESS,RESERVATION_PROCESS,FINE_MANAGE";
            case ADMIN -> "USER_MANAGE,ROLE_MANAGE,SYSTEM_CONFIG,LOG_VIEW,BACKUP_RESTORE,REPORT_VIEW";
        };
    }

    private List<String> splitPermissionScope(String permissionScope) {
        if (permissionScope == null || permissionScope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(permissionScope.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
