package com.team.lms.entity;

import com.team.lms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    private String description;
}
