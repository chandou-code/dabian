package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 系统配置Mapper接口
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 获取所有公开配置
     */
    Map<String, String> selectPublicConfigs();

    /**
     * 获取所有配置
     */
    Map<String, String> selectAllConfigs();

    /**
     * 批量更新配置
     */
    int batchUpdateConfigs(@Param("configs") Map<String, String> configs);

    /**
     * 根据键查询配置
     */
    SystemConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 更新配置值
     */
    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}