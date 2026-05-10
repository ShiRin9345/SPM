package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserVo {
    private Long userId;
    private String username;
    private String password;
    private String fullName;
    private String studentNo;
    private String phone;
    private String role;
    private Boolean enabled;
    private String status;
}
