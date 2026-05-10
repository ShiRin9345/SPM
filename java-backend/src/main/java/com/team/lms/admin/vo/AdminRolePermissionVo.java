package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRolePermissionVo {
    private String role;
    private String permissionScope;
}
