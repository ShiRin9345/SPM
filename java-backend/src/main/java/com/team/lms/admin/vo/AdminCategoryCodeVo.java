package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCategoryCodeVo {
    private Long categoryId;
    private String code;
    private String name;
    private Boolean enabled;
}
