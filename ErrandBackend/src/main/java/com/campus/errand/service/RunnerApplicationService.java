package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.RunnerApplication;

import java.util.List;
import java.util.Map;

/**
 * 跑腿员申请服务接口
 */
public interface RunnerApplicationService extends IService<RunnerApplication> {

    /**
     * 提交跑腿员申请
     * @param application 申请信息
     * @return 申请结果
     */
    boolean submitApplication(RunnerApplication application);

    /**
     * 获取申请列表
     * @param params 查询参数
     * @return 申请列表
     */
    List<RunnerApplication> getApplicationList(Map<String, Object> params);

    /**
     * 获取申请总数
     * @param params 查询参数
     * @return 申请总数
     */
    long getApplicationCount(Map<String, Object> params);

    /**
     * 根据ID获取申请详情
     * @param id 申请ID
     * @return 申请详情
     */
    RunnerApplication getApplicationById(Long id);

    /**
     * 根据用户ID获取申请
     * @param userId 用户ID
     * @return 申请信息
     */
    RunnerApplication getApplicationByUserId(Long userId);

    /**
     * 批准申请
     * @param id 申请ID
     * @param reviewerId 审核人ID
     * @param comment 审核意见
     * @return 操作结果
     */
    boolean approveApplication(Long id, Long reviewerId, String comment);

    /**
     * 拒绝申请
     * @param id 申请ID
     * @param reviewerId 审核人ID
     * @param comment 审核意见
     * @return 操作结果
     */
    boolean rejectApplication(Long id, Long reviewerId, String comment);
}
