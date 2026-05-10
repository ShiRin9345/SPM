package com.team.lms.entity;

import com.team.lms.common.enums.RoleType;
import com.team.lms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RolePermission extends BaseEntity {
    private RoleType role;
    private String permissionScope;
}
