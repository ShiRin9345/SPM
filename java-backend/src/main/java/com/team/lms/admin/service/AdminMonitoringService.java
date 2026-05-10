package com.team.lms.admin.service;

import com.team.lms.admin.vo.AdminBackupRecordVo;
import com.team.lms.admin.vo.AdminMonitoringOverviewVo;
import com.team.lms.admin.vo.AdminOperationLogVo;

import java.util.List;

public interface AdminMonitoringService {
    AdminMonitoringOverviewVo getOverview(String authorizationHeader);
    List<AdminOperationLogVo> listOperationLogs(String authorizationHeader);
    List<AdminOperationLogVo> listAbnormalBehaviors(String authorizationHeader);
    AdminBackupRecordVo executeBackup(String authorizationHeader);
    List<AdminBackupRecordVo> listBackupRecords(String authorizationHeader);
}
