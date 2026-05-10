package com.team.lms.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminMonitoringOverviewVo {
    private Integer totalUsers;
    private Integer totalBooks;
    private Integer activeBorrows;
    private Integer pendingReservations;
    private Integer unpaidFines;
    private String systemStatus;
    private String latestBackup;
}
