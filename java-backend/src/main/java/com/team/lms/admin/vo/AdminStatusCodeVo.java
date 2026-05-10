package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatusCodeVo {
    private Long statusCodeId;
    private String codeType;
    private String codeValue;
    private String displayName;
    private String description;
    private Boolean enabled;
}
