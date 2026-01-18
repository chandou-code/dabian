package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Notification;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserReport;
import com.campus.errand.mapper.UserReportMapper;
import com.campus.errand.service.NotificationService;
import com.campus.errand.service.UserReportService;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户举报服务实现类
 */
@Slf4j
@Service
public class UserReportServiceImpl implements UserReportService {
    
    @Autowired
    private UserReportMapper userReportMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public UserReport createReport(UserReport report) {
        // 设置默认值
        report.setStatus("pending");
        report.setCreatedAt(new Date());
        report.setUpdatedAt(new Date());
        
        // 保存举报
        int result = userReportMapper.insert(report);
        if (result > 0) {
            log.info("举报创建成功: {}, 举报者: {}, 被举报者: {}", report.getId(), report.getReporterId(), report.getReportedUserId());
            
            // 发送举报受理通知
            Notification notification = new Notification();
            notification.setUserId(report.getReporterId());
            notification.setTitle("举报受理通知");
            notification.setContent("您的举报已成功提交，我们将尽快处理。");
            notification.setType("system");
            notification.setTagName("system");
            notification.setNoticeId(report.getId());
            notification.setRead(false);
            notification.setCreatedAt(new Date());
            notificationService.createNotification(notification);
            
            return report;
        } else {
            log.error("举报创建失败: 举报者: {}, 被举报者: {}", report.getReporterId(), report.getReportedUserId());
            return null;
        }
    }
    
    @Override
    public Page<UserReport> getReportList(int page, int pageSize, String status, String keyword) {
        Page<UserReport> reportPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserReport> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(UserReport::getStatus, status);
        }
        
        // 按关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(UserReport::getContent, keyword)
                .or().like(UserReport::getAdminRemark, keyword)
            );
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(UserReport::getCreatedAt);
        
        // 获取举报列表
        Page<UserReport> resultPage = userReportMapper.selectPage(reportPage, queryWrapper);
        
        // 为每个举报对象填充举报人和被举报人的姓名
        fillUserNames(resultPage.getRecords());
        
        return resultPage;
    }
    
    @Override
    public Page<UserReport> getUserReportList(Long userId, int page, int pageSize) {
        Page<UserReport> reportPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserReport> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询用户的举报
        queryWrapper.eq(UserReport::getReporterId, userId);
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(UserReport::getCreatedAt);
        
        // 获取举报列表
        Page<UserReport> resultPage = userReportMapper.selectPage(reportPage, queryWrapper);
        
        // 为每个举报对象填充举报人和被举报人的姓名
        fillUserNames(resultPage.getRecords());
        
        return resultPage;
    }
    
    @Override
    public UserReport getReportById(Long id) {
        UserReport report = userReportMapper.selectById(id);
        if (report != null) {
            // 为单个举报对象填充举报人和被举报人的姓名
            List<UserReport> reports = new ArrayList<>();
            reports.add(report);
            fillUserNames(reports);
        }
        return report;
    }
    
    @Override
    public boolean updateReportStatus(Long id, String status, String adminRemark) {
        UserReport report = userReportMapper.selectById(id);
        if (report == null) {
            log.warn("举报不存在: {}", id);
            return false;
        }
        
        // 保存旧状态用于日志记录
        String oldStatus = report.getStatus();
        
        // 更新举报状态
        report.setStatus(status);
        report.setAdminRemark(adminRemark);
        report.setHandledAt(new Date());
        report.setUpdatedAt(new Date());
        
        int result = userReportMapper.updateById(report);
        if (result > 0) {
            log.info("举报状态更新成功: {}, 旧状态: {}, 新状态: {}", id, oldStatus, status);
            
            // 发送举报进展/结果通知
            Notification notification = new Notification();
            notification.setUserId(report.getReporterId());
            notification.setType("system");
            notification.setTagName("system");
            notification.setNoticeId(report.getId());
            notification.setRead(false);
            notification.setCreatedAt(new Date());
            
            // 根据状态设置不同的通知内容
            if (status.equals("processed")) {
                notification.setTitle("举报处理结果");
                notification.setContent("您的举报已处理完成。处理结果：已处理。" + (adminRemark != null && !adminRemark.isEmpty() ? "备注：" + adminRemark : ""));
            } else if (status.equals("dismissed")) {
                notification.setTitle("举报处理结果");
                notification.setContent("您的举报已处理完成。处理结果：已驳回。" + (adminRemark != null && !adminRemark.isEmpty() ? "备注：" + adminRemark : ""));
            } else if (status.equals("pending")) {
                notification.setTitle("举报进展通知");
                notification.setContent("您的举报已被重新标记为待处理，我们将继续跟进。");
            }
            
            notificationService.createNotification(notification);
            
            return true;
        } else {
            log.error("举报状态更新失败: {}, 新状态: {}", id, status);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getReportStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总举报数
        LambdaQueryWrapper<UserReport> totalWrapper = new LambdaQueryWrapper<>();
        long total = userReportMapper.selectCount(totalWrapper);
        
        // 获取待处理举报数
        LambdaQueryWrapper<UserReport> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(UserReport::getStatus, "pending");
        long pending = userReportMapper.selectCount(pendingWrapper);
        
        // 获取已处理举报数（包括processed和dismissed状态）
        LambdaQueryWrapper<UserReport> processedWrapper = new LambdaQueryWrapper<>();
        processedWrapper.in(UserReport::getStatus, "processed", "dismissed");
        long processed = userReportMapper.selectCount(processedWrapper);
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("processed", processed);
        
        return stats;
    }
    
    /**
     * 为举报列表填充举报人和被举报人的姓名
     * @param reports 举报列表
     */
    private void fillUserNames(List<UserReport> reports) {
        for (UserReport report : reports) {
            // 填充举报者姓名
            User reporter = userService.getUserById(report.getReporterId());
            if (reporter != null) {
                // 优先使用昵称，其次是真实姓名，最后是用户名
                report.setReporterName(reporter.getNickname() != null ? reporter.getNickname() : 
                                     (reporter.getRealName() != null ? reporter.getRealName() : 
                                      reporter.getUsername()));
            }
            
            // 填充被举报者姓名
            User reportedUser = userService.getUserById(report.getReportedUserId());
            if (reportedUser != null) {
                // 优先使用昵称，其次是真实姓名，最后是用户名
                report.setReportedName(reportedUser.getNickname() != null ? reportedUser.getNickname() : 
                                      (reportedUser.getRealName() != null ? reportedUser.getRealName() : 
                                       reportedUser.getUsername()));
            }
        }
    }
}