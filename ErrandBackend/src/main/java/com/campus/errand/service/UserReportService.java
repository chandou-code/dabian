package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.UserReport;
import java.util.Map;

/**
 * 用户举报服务接口
 */
public interface UserReportService {
    
    /**
     * 创建举报
     * @param report 举报信息
     * @return 举报对象
     */
    UserReport createReport(UserReport report);
    
    /**
     * 获取举报列表
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param status 举报状态
     * @param keyword 搜索关键词
     * @return 举报列表
     */
    Page<UserReport> getReportList(int page, int pageSize, String status, String keyword);
    
    /**
     * 获取用户的举报列表
     * @param userId 用户ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 举报列表
     */
    Page<UserReport> getUserReportList(Long userId, int page, int pageSize);
    
    /**
     * 获取举报详情
     * @param id 举报ID
     * @return 举报详情
     */
    UserReport getReportById(Long id);
    
    /**
     * 更新举报状态
     * @param id 举报ID
     * @param status 新状态
     * @param adminRemark 管理员备注
     * @return 是否更新成功
     */
    boolean updateReportStatus(Long id, String status, String adminRemark);
    
    /**
     * 获取举报统计数据
     * @return 统计数据map
     */
    Map<String, Object> getReportStats();
}