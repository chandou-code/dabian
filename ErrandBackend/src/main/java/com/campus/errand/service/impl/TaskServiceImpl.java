package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.ErrandTask;
import com.campus.errand.mapper.ErrandTaskMapper;
import com.campus.errand.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 任务服务实现类
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ErrandTaskMapper taskMapper;

    @Override
    public ErrandTask publishTask(ErrandTask task) {
        // 设置默认值
        task.setStatus("pending");
        task.setPublishTime(new Date());
        
        // 生成任务编号
        String taskNo = "T" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + 
                       String.format("%06d", (int)(Math.random() * 1000000));
        task.setTaskNo(taskNo);

        // 保存任务
        int result = taskMapper.insert(task);
        if (result > 0) {
            log.info("任务发布成功: {}, 发布人: {}", task.getTitle(), task.getPublisherId());
            return taskMapper.selectById(task.getId());
        } else {
            log.error("任务发布失败: {}", task.getTitle());
            return null;
        }
    }

    @Override
    public Page<ErrandTask> getTaskList(int page, int pageSize, String status, String type, String keyword) {
        Page<ErrandTask> taskPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ErrandTask> queryWrapper = new LambdaQueryWrapper<>();

        // 根据传入的status参数过滤任务状态
        if (status != null && !status.isEmpty() && !"null".equals(status)) {
            queryWrapper.eq(ErrandTask::getStatus, status);
        } else {
            // 默认显示所有状态的任务（管理员统计时需要）
        }

        // 类型过滤
        if (type != null && !type.isEmpty() && !"null".equals(type)) {
            queryWrapper.eq(ErrandTask::getType, type);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty() && !"null".equals(keyword)) {
            queryWrapper.like(ErrandTask::getTitle, keyword)
                    .or().like(ErrandTask::getDescription, keyword);
        }

        // 按发布时间倒序排序
        queryWrapper.orderByDesc(ErrandTask::getPublishTime);

        Page<ErrandTask> result = taskMapper.selectPage(taskPage, queryWrapper);
        
        // 修复total计算问题
        if (result.getRecords() != null && !result.getRecords().isEmpty() && result.getTotal() == 0) {
            // 如果查询到记录但total为0，手动计算总数
            LambdaQueryWrapper<ErrandTask> countQuery = new LambdaQueryWrapper<>();
            if (status != null && !status.isEmpty() && !"null".equals(status)) {
                countQuery.eq(ErrandTask::getStatus, status);
            }
            if (type != null && !type.isEmpty() && !"null".equals(type)) {
                countQuery.eq(ErrandTask::getType, type);
            }
            if (keyword != null && !keyword.isEmpty() && !"null".equals(keyword)) {
                countQuery.like(ErrandTask::getTitle, keyword)
                        .or().like(ErrandTask::getDescription, keyword);
            }
            long total = taskMapper.selectCount(countQuery);
            result.setTotal(total);
        }
        
        return result;
    }

    @Override
    public Page<ErrandTask> getUserTasks(Long userId, int page, int pageSize, String status) {
        Page<ErrandTask> taskPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ErrandTask> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID过滤 - 只查询用户作为发布者的任务
        queryWrapper.eq(ErrandTask::getPublisherId, userId);
        
        // 只查询已被接单的任务（runnerId不为空）
        queryWrapper.isNotNull(ErrandTask::getRunnerId);

        // 状态过滤
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ErrandTask::getStatus, status);
        }

        // 按发布时间倒序排序
        queryWrapper.orderByDesc(ErrandTask::getPublishTime);

        return taskMapper.selectPage(taskPage, queryWrapper);
    }
    
    @Override
    public Page<ErrandTask> getRunnerTasks(Long runnerId, int page, int pageSize, String status) {
        Page<ErrandTask> taskPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ErrandTask> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询跑腿员自己接的任务
        queryWrapper.eq(ErrandTask::getRunnerId, runnerId);

        // 状态过滤
        if (status != null && !status.isEmpty()) {
            if (status.contains(",")) {
                // 多状态查询
                String[] statusArray = status.split(",");
                queryWrapper.in(ErrandTask::getStatus, statusArray);
            } else {
                // 单状态查询
                queryWrapper.eq(ErrandTask::getStatus, status);
            }
        }

        // 按发布时间倒序排序
        queryWrapper.orderByDesc(ErrandTask::getPublishTime);

        Page<ErrandTask> result = taskMapper.selectPage(taskPage, queryWrapper);
        
        // 修复total计算问题
        if (result.getRecords() != null && !result.getRecords().isEmpty() && result.getTotal() == 0) {
            // 如果查询到记录但total为0，手动计算总数
            LambdaQueryWrapper<ErrandTask> countQuery = new LambdaQueryWrapper<>();
            countQuery.eq(ErrandTask::getRunnerId, runnerId);
            if (status != null && !status.isEmpty()) {
                if (status.contains(",")) {
                    String[] statusArray = status.split(",");
                    countQuery.in(ErrandTask::getStatus, statusArray);
                } else {
                    countQuery.eq(ErrandTask::getStatus, status);
                }
            }
            long total = taskMapper.selectCount(countQuery);
            result.setTotal(total);
        }
        
        return result;
    }

    @Override
    public ErrandTask getTaskById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public boolean updateTaskStatus(Long id, String status, Long userId) {
        ErrandTask task = taskMapper.selectById(id);
        if (task == null) {
            log.warn("任务不存在: {}", id);
            return false;
        }

        // 检查权限 - 如果userId为null，说明是管理员操作，跳过权限检查
        if (userId != null && !task.getPublisherId().equals(userId) && (task.getRunnerId() == null || !task.getRunnerId().equals(userId))) {
            log.warn("无权限操作任务: {}, 操作人: {}", id, userId);
            return false;
        }

        // 更新状态
        task.setStatus(status);

        int result = taskMapper.updateById(task);
        if (result > 0) {
            log.info("任务状态更新成功: {}, 状态: {}", id, status);
            return true;
        } else {
            log.error("任务状态更新失败: {}", id);
            return false;
        }
    }

    @Override
    public ErrandTask updateTask(ErrandTask task) {
        ErrandTask existingTask = taskMapper.selectById(task.getId());
        if (existingTask == null) {
            log.warn("任务不存在: {}", task.getId());
            return null;
        }

        // 检查权限
        if (!existingTask.getPublisherId().equals(task.getPublisherId())) {
            log.warn("无权限更新任务: {}, 操作人: {}", task.getId(), task.getPublisherId());
            return null;
        }

        int result = taskMapper.updateById(task);
        if (result > 0) {
            log.info("任务更新成功: {}", task.getId());
            return taskMapper.selectById(task.getId());
        } else {
            log.error("任务更新失败: {}", task.getId());
            return null;
        }
    }

    @Override
    public boolean deleteTask(Long id, Long userId) {
        ErrandTask task = taskMapper.selectById(id);
        if (task == null) {
            log.warn("任务不存在: {}", id);
            return false;
        }

        // 检查权限 - 如果userId为null，说明是管理员操作，跳过权限检查
        if (userId != null && !task.getPublisherId().equals(userId)) {
            log.warn("无权限删除任务: {}, 操作人: {}", id, userId);
            return false;
        }

        // 管理员可以删除任何状态的任务，普通用户只能删除pending状态的任务
        if (userId != null && !"pending".equals(task.getStatus())) {
            log.warn("任务状态不允许删除: {}, 状态: {}", id, task.getStatus());
            return false;
        }

        int result = taskMapper.deleteById(id);
        if (result > 0) {
            log.info("任务删除成功: {}", id);
            return true;
        } else {
            log.error("任务删除失败: {}", id);
            return false;
        }
    }

    @Override
    public boolean acceptTask(Long taskId, Long runnerId) {
        ErrandTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }

        // 检查任务状态
        if (!"pending".equals(task.getStatus())) {
            log.warn("任务状态不允许接受: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }

        // 检查是否是自己发布的任务
        if (task.getPublisherId().equals(runnerId)) {
            log.warn("不能接受自己发布的任务: {}, 操作人: {}", taskId, runnerId);
            return false;
        }

        // 更新任务
        task.setStatus("accepted");
        task.setRunnerId(runnerId);
        task.setAcceptTime(new Date());

        int result = taskMapper.updateById(task);
        if (result > 0) {
            log.info("任务接受成功: {}, 接单人: {}", taskId, runnerId);
            return true;
        } else {
            log.error("任务接受失败: {}", taskId);
            return false;
        }
    }

    @Override
    public boolean completeTask(Long taskId, Long userId) {
        ErrandTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }

        // 检查任务状态
        if (!"accepted".equals(task.getStatus()) && !"delivering".equals(task.getStatus())) {
            log.warn("任务状态不允许完成: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }

        // 检查权限
        if (!task.getRunnerId().equals(userId)) {
            log.warn("无权限完成任务: {}, 操作人: {}", taskId, userId);
            return false;
        }

        // 更新任务
        task.setStatus("completed");
        task.setCompleteTime(new Date());

        int result = taskMapper.updateById(task);
        if (result > 0) {
            log.info("任务完成成功: {}", taskId);
            return true;
        } else {
            log.error("任务完成失败: {}", taskId);
            return false;
        }
    }

    @Override
    public boolean cancelTask(Long taskId, Long userId) {
        ErrandTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return false;
        }

        // 管理员可以取消任何状态的任务，普通用户只能取消pending或accepted状态的任务
        if (userId != null && !"pending".equals(task.getStatus()) && !"accepted".equals(task.getStatus())) {
            log.warn("任务状态不允许取消: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }

        // 检查权限 - 如果userId为null，说明是管理员操作，跳过权限检查
        if (userId != null && !task.getPublisherId().equals(userId) && (task.getRunnerId() == null || !task.getRunnerId().equals(userId))) {
            log.warn("无权限取消任务: {}, 操作人: {}", taskId, userId);
            return false;
        }

        // 更新任务
        task.setStatus("cancelled");
        task.setCancelTime(new Date());

        int result = taskMapper.updateById(task);
        if (result > 0) {
            log.info("任务取消成功: {}", taskId);
            return true;
        } else {
            log.error("任务取消失败: {}", taskId);
            return false;
        }
    }

    @Override
    public java.util.Map<String, Integer> getUserOrderStats(Long userId) {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        // 初始化各状态订单数量为0
        stats.put("pending", 0);
        stats.put("accepted", 0);
        stats.put("delivering", 0);
        stats.put("completed", 0);
        stats.put("cancelled", 0);
        stats.put("total", 0);
        
        // 构建查询条件 - 只查询用户发布的已被接单订单
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErrandTask> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ErrandTask::getPublisherId, userId)
                .isNotNull(ErrandTask::getRunnerId); // 只统计已被接单的任务
        
        // 查询所有相关任务
        java.util.List<ErrandTask> tasks = taskMapper.selectList(queryWrapper);
        
        // 统计各状态订单数量
        for (ErrandTask task : tasks) {
            String status = task.getStatus();
            stats.put(status, stats.getOrDefault(status, 0) + 1);
            stats.put("total", stats.getOrDefault("total", 0) + 1);
        }
        
        return stats;
    }
}
