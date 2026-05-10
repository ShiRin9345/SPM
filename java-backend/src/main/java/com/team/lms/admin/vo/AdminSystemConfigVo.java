package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminSystemConfigVo {
    private String configKey;
    private String configValue;
    private String description;
}
