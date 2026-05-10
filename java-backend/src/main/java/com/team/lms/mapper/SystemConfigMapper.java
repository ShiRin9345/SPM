package com.team.lms.mapper;

import com.team.lms.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemConfigMapper {
    List<SystemConfig> selectAll();
    SystemConfig selectByKey(@Param("configKey") String configKey);
    int insert(SystemConfig systemConfig);
    int update(SystemConfig systemConfig);
}
