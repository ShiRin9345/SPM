package com.team.lms.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.lms.admin.service.AdminMonitoringService;
import com.team.lms.admin.vo.AdminBackupRecordVo;
import com.team.lms.admin.vo.AdminMonitoringOverviewVo;
import com.team.lms.admin.vo.AdminOperationLogVo;
import com.team.lms.common.enums.BorrowRecordStatus;
import com.team.lms.common.enums.FineStatus;
import com.team.lms.common.enums.RoleType;
import com.team.lms.common.support.CurrentUserSupport;
import com.team.lms.common.support.OperationLogSupport;
import com.team.lms.entity.BackupRecord;
import com.team.lms.entity.OperationLog;
import com.team.lms.entity.User;
import com.team.lms.exception.BusinessException;
import com.team.lms.mapper.BackupRecordMapper;
import com.team.lms.mapper.BookMapper;
import com.team.lms.mapper.BorrowRecordMapper;
import com.team.lms.mapper.FineMapper;
import com.team.lms.mapper.OperationLogMapper;
import com.team.lms.mapper.ReservationMapper;
import com.team.lms.mapper.SystemConfigMapper;
import com.team.lms.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMonitoringServiceImpl implements AdminMonitoringService {

    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final ReservationMapper reservationMapper;
    private final FineMapper fineMapper;
    private final OperationLogMapper operationLogMapper;
    private final BackupRecordMapper backupRecordMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final CurrentUserSupport currentUserSupport;
    private final OperationLogSupport operationLogSupport;
    private final ObjectMapper objectMapper;

    @Override
    public AdminMonitoringOverviewVo getOverview(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        BackupRecord latestBackup = backupRecordMapper.selectLatest();
        return AdminMonitoringOverviewVo.builder()
                .totalUsers(userMapper.selectAllActive().size())
                .totalBooks(bookMapper.selectAll().size())
                .activeBorrows((int) borrowRecordMapper.selectAll().stream()
                        .filter(item -> item.getStatus() == BorrowRecordStatus.BORROWED
                                || item.getStatus() == BorrowRecordStatus.RETURN_PENDING
                                || item.getStatus() == BorrowRecordStatus.OVERDUE)
                        .count())
                .pendingReservations(reservationMapper.selectPending().size())
                .unpaidFines((int) fineMapper.selectAll().stream()
                        .filter(item -> item.getStatus() == FineStatus.UNPAID)
                        .count())
                .systemStatus("Healthy")
                .latestBackup(latestBackup == null ? "No backup yet" : latestBackup.getBackupName())
                .build();
    }

    @Override
    public List<AdminOperationLogVo> listOperationLogs(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return operationLogMapper.selectAll().stream().map(this::toOperationLogVo).toList();
    }

    @Override
    public List<AdminOperationLogVo> listAbnormalBehaviors(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return operationLogMapper.selectAll().stream()
                .filter(item -> {
                    String action = item.getActionName() == null ? "" : item.getActionName().toUpperCase();
                    String message = item.getResultMessage() == null ? "" : item.getResultMessage().toLowerCase();
                    return action.contains("FAILED")
                            || message.contains("invalid")
                            || message.contains("denied")
                            || message.contains("disabled")
                            || message.contains("not found");
                })
                .map(this::toOperationLogVo)
                .toList();
    }

    @Override
    public AdminBackupRecordVo executeBackup(String authorizationHeader) {
        User admin = requireAdmin(authorizationHeader);
        try {
            Files.createDirectories(Path.of("backups"));
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String backupName = "backup-" + stamp;
            Path file = Path.of("backups", backupName + ".json");

            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("createdAt", LocalDateTime.now().toString());
            snapshot.put("users", userMapper.selectAllActive().size());
            snapshot.put("books", bookMapper.selectAll().size());
            snapshot.put("borrowRecords", borrowRecordMapper.selectAll().size());
            snapshot.put("pendingReservations", reservationMapper.selectPending().size());
            snapshot.put("configs", systemConfigMapper.selectAll());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), snapshot);

            BackupRecord backupRecord = new BackupRecord();
            backupRecord.setBackupName(backupName);
            backupRecord.setFilePath(file.toAbsolutePath().toString());
            backupRecord.setStatus("SUCCESS");
            backupRecord.setSummary("Users=" + snapshot.get("users") + ", Books=" + snapshot.get("books") + ", BorrowRecords=" + snapshot.get("borrowRecords"));
            backupRecordMapper.insert(backupRecord);

            operationLogSupport.record("SYSTEM_BACKUP", "BACKUP_EXECUTE", admin.getUsername(), "Backup created at " + backupRecord.getFilePath());
            return toBackupVo(backupRecordMapper.selectLatest());
        } catch (IOException exception) {
            operationLogSupport.record("SYSTEM_BACKUP", "BACKUP_FAILED", admin.getUsername(), exception.getMessage());
            throw new BusinessException(500, "failed to execute backup");
        }
    }

    @Override
    public List<AdminBackupRecordVo> listBackupRecords(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return backupRecordMapper.selectAll().stream().map(this::toBackupVo).toList();
    }

    private User requireAdmin(String authorizationHeader) {
        User user = currentUserSupport.requireUser(authorizationHeader);
        if (user.getRole() != RoleType.ADMIN) {
            throw new BusinessException(403, "current user is not an admin");
        }
        return user;
    }

    private AdminOperationLogVo toOperationLogVo(OperationLog operationLog) {
        return AdminOperationLogVo.builder()
                .logId(operationLog.getId())
                .moduleName(operationLog.getModuleName())
                .actionName(operationLog.getActionName())
                .operatorName(operationLog.getOperatorName())
                .resultMessage(operationLog.getResultMessage())
                .createdAt(operationLog.getCreatedAt() == null ? null : operationLog.getCreatedAt().toString())
                .build();
    }

    private AdminBackupRecordVo toBackupVo(BackupRecord backupRecord) {
        return AdminBackupRecordVo.builder()
                .backupId(backupRecord.getId())
                .backupName(backupRecord.getBackupName())
                .filePath(backupRecord.getFilePath())
                .status(backupRecord.getStatus())
                .summary(backupRecord.getSummary())
                .createdAt(backupRecord.getCreatedAt() == null ? null : backupRecord.getCreatedAt().toString())
                .build();
    }
}
