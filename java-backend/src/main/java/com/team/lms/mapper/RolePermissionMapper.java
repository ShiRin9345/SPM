package com.team.lms.mapper;

import com.team.lms.common.enums.RoleType;
import com.team.lms.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    List<RolePermission> selectAll();
    RolePermission selectByRole(@Param("role") RoleType role);
    int insert(RolePermission rolePermission);
    int update(RolePermission rolePermission);
}
