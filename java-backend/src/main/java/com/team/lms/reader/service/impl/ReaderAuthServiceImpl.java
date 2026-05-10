package com.team.lms.reader.service.impl;

import com.team.lms.common.enums.RoleType;
import com.team.lms.common.support.OperationLogSupport;
import com.team.lms.entity.User;
import com.team.lms.exception.BusinessException;
import com.team.lms.mapper.UserMapper;
import com.team.lms.reader.dto.ReaderLoginRequest;
import com.team.lms.reader.dto.ReaderRegisterRequest;
import com.team.lms.reader.service.ReaderAuthService;
import com.team.lms.reader.vo.ReaderAuthVo;
import org.springframework.stereotype.Service;

@Service
public class ReaderAuthServiceImpl implements ReaderAuthService {

    private final UserMapper userMapper;
    private final OperationLogSupport operationLogSupport;

    public ReaderAuthServiceImpl(UserMapper userMapper, OperationLogSupport operationLogSupport) {
        this.userMapper = userMapper;
        this.operationLogSupport = operationLogSupport;
    }

    @Override
    public ReaderAuthVo register(ReaderRegisterRequest request) {
        User existing = userMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(400, "username already exists");
        }

        User existingStudentNo = userMapper.selectByStudentNo(request.getStudentNo());
        if (existingStudentNo != null) {
            throw new BusinessException(400, "studentNo already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setStudentNo(request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setRole(RoleType.READER);
        user.setEnabled(true);
        userMapper.insert(user);

        return ReaderAuthVo.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .token("mock-token-for-" + user.getUsername())
                .build();
    }

    @Override
    public ReaderAuthVo login(ReaderLoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            operationLogSupport.record("AUTH", "LOGIN_FAILED", request.getUsername(), "user not found");
            throw new BusinessException(404, "user not found");
        }
        RoleType expectedRole;
        try {
            expectedRole = RoleType.valueOf(request.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            operationLogSupport.record("AUTH", "LOGIN_FAILED", request.getUsername(), "invalid role");
            throw new BusinessException(400, "role must be READER/LIBRARIAN/ADMIN");
        }
        if (user.getRole() != expectedRole) {
            operationLogSupport.record("AUTH", "LOGIN_FAILED", request.getUsername(), "selected workspace role does not match current user role");
            throw new BusinessException(403, "selected workspace role does not match current user role");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            operationLogSupport.record("AUTH", "LOGIN_FAILED", request.getUsername(), "user is disabled");
            throw new BusinessException(403, "user is disabled");
        }
        if (!request.getPassword().equals(user.getPassword())) {
            operationLogSupport.record("AUTH", "LOGIN_FAILED", request.getUsername(), "username or password is invalid");
            throw new BusinessException(400, "username or password is invalid");
        }
        operationLogSupport.record("AUTH", "LOGIN_SUCCESS", request.getUsername(), "login success");
        return ReaderAuthVo.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .token("mock-token-for-" + user.getUsername())
                .build();
    }
}
