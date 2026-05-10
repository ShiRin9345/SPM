package com.team.lms.entity;

import com.team.lms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatusCode extends BaseEntity {
    private String codeType;
    private String codeValue;
    private String displayName;
    private String description;
    private Boolean enabled;
}
