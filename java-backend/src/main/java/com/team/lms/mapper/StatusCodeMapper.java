package com.team.lms.mapper;

import com.team.lms.entity.StatusCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatusCodeMapper {
    List<StatusCode> selectAll();
    List<StatusCode> selectByType(@Param("codeType") String codeType);
    StatusCode selectById(Long id);
    StatusCode selectByTypeAndValue(@Param("codeType") String codeType, @Param("codeValue") String codeValue);
    int insert(StatusCode statusCode);
    int update(StatusCode statusCode);
}
