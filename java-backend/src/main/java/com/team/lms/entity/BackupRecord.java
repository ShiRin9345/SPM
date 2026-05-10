package com.team.lms.entity;

import com.team.lms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BackupRecord extends BaseEntity {
    private String backupName;
    private String filePath;
    private String status;
    private String summary;
}
