package com.team.lms.mapper;

import com.team.lms.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OperationLogMapper {
    List<OperationLog> selectAll();
    int insert(OperationLog operationLog);
}
