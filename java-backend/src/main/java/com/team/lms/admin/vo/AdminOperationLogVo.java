package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOperationLogVo {
    private Long logId;
    private String moduleName;
    private String actionName;
    private String operatorName;
    private String resultMessage;
    private String createdAt;
}
