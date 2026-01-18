package com.campus.errand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.errand.entity.RunnerApplication;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 跑腿员申请表Mapper接口
 */
@Mapper
public interface RunnerApplicationMapper extends BaseMapper<RunnerApplication> {

    /**
     * 获取申请列表，支持分页和状态筛选
     * @param params 查询参数
     * @return 申请列表
     */
    List<RunnerApplication> getApplicationList(Map<String, Object> params);

    /**
     * 获取申请总数
     * @param params 查询参数
     * @return 申请总数
     */
    Integer getApplicationCount(Map<String, Object> params);

    /**
     * 根据用户ID获取申请
     * @param userId 用户ID
     * @return 申请信息
     */
    RunnerApplication getApplicationByUserId(Long userId);
}
