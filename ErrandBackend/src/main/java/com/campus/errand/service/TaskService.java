package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.ErrandTask;

import java.util.List;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 发布任务
     * @param task 任务信息
     * @return 发布后的任务信息
     */
    ErrandTask publishTask(ErrandTask task);

    /**
     * 获取任务列表
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param status 任务状态（可选）
     * @param type 任务类型（可选）
     * @param keyword 搜索关键词（可选）
     * @return 任务列表
     */
    Page<ErrandTask> getTaskList(int page, int pageSize, String status, String type, String keyword);

    /**
     * 获取用户发布的任务列表
     * @param userId 用户ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param status 任务状态（可选）
     * @return 任务列表
     */
    Page<ErrandTask> getUserTasks(Long userId, int page, int pageSize, String status);
    
    /**
     * 获取跑腿员接的任务列表
     * @param runnerId 跑腿员ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param status 任务状态（可选）
     * @return 任务列表
     */
    Page<ErrandTask> getRunnerTasks(Long runnerId, int page, int pageSize, String status);

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    ErrandTask getTaskById(Long id);

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 新状态
     * @param userId 操作人ID
     * @return 是否更新成功
     */
    boolean updateTaskStatus(Long id, String status, Long userId);

    /**
     * 更新任务信息
     * @param task 任务信息
     * @return 更新后的任务信息
     */
    ErrandTask updateTask(ErrandTask task);

    /**
     * 删除任务
     * @param id 任务ID
     * @param userId 操作人ID
     * @return 是否删除成功
     */
    boolean deleteTask(Long id, Long userId);

    /**
     * 接受任务
     * @param taskId 任务ID
     * @param runnerId 接单人ID
     * @return 是否接受成功
     */
    boolean acceptTask(Long taskId, Long runnerId);

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param userId 操作人ID
     * @return 是否完成成功
     */
    boolean completeTask(Long taskId, Long userId);

    /**
     * 取消任务
     * @param taskId 任务ID
     * @param userId 操作人ID
     * @return 是否取消成功
     */
    boolean cancelTask(Long taskId, Long userId);

    /**
     * 获取用户订单统计数据
     * @param userId 用户ID
     * @return 各状态订单数量统计
     */
    java.util.Map<String, Integer> getUserOrderStats(Long userId);
}
