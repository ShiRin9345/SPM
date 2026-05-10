package com.team.lms.common.support;

import com.team.lms.entity.OperationLog;
import com.team.lms.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationLogSupport {

    private final OperationLogMapper operationLogMapper;

    public void record(String moduleName, String actionName, String operatorName, String resultMessage) {
        OperationLog operationLog = new OperationLog();
        operationLog.setModuleName(moduleName);
        operationLog.setActionName(actionName);
        operationLog.setOperatorName(operatorName == null || operatorName.isBlank() ? "system" : operatorName);
        operationLog.setResultMessage(resultMessage);
        operationLogMapper.insert(operationLog);
    }
}
