package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.common.constants.StatusConstants;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.StatisticsService;
import com.campus.lostfound.service.SystemConfigService;
import com.campus.lostfound.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@Slf4j

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private StatisticsService statisticsService;
    
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        
        try {
            var page = userService.getUserPage(current, pageSize, keyword, role, status);
            
            Map<String, Object> result = Map.of(
                "users", page.getRecords(),
                "pagination", Map.of(
                    "current", page.getCurrent(),
                    "pageSize", page.getSize(),
                    "total", page.getTotal(),
                    "pages", page.getPages()
                )
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/status")
    public Result updateUserStatus(@PathVariable Long id,
                                  @RequestBody Map<String, Integer> request,
                                  @RequestHeader("Authorization") String token) {
        try {
            Integer status = request.get("status");
            if (status == null || (status != StatusConstants.USER_ENABLED && status != StatusConstants.USER_DISABLED)) {
                return Result.error("状态值无效");
            }
            
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/users/{id}/reset-password")
    public Result<Map<String, String>> resetPassword(@PathVariable Long id,
                                                    @RequestHeader("Authorization") String token) {
        try {
            String newPassword = userService.resetPassword(id);
            if (newPassword != null) {
                return Result.success("密码重置成功", Map.of("newPassword", newPassword));
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码失败", e);
            return Result.error("重置失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/users/batch")
    public Result<Map<String, Object>> batchOperateUsers(@RequestBody Map<String, Object> request,
                                                        @RequestHeader("Authorization") String token) {
        try {
            @SuppressWarnings("unchecked")
            Long[] userIds = ((List<Integer>) request.get("userIds"))
                .stream()
                .map(Long::valueOf)
                .toArray(Long[]::new);
            
            String action = (String) request.get("action");
            
            boolean success = userService.batchOperateUsers(userIds, action);
            if (success) {
                return Result.success("批量操作完成", Map.of(
                    "successCount", userIds.length,
                    "failCount", 0
                ));
            } else {
                return Result.error("批量操作失败");
            }
        } catch (Exception e) {
            log.error("批量操作用户失败", e);
            return Result.error("批量操作失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}")
    public Result updateUserInfo(@PathVariable Long id,
                               @RequestBody User userInfo,
                               @RequestHeader("Authorization") String token) {
        try {
            boolean success = userService.updateUserInfo(id, userInfo);
            if (success) {
                return Result.success("用户信息更新成功");
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAdminStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String timeRange,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> stats = itemService.getAdminStatistics(startDate, endDate, timeRange);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取管理员统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/system/config")
    public Result<Map<String, Object>> getSystemConfig(@RequestHeader("Authorization") String token) {
        try {
            Map<String, String> configs = systemConfigService.getAllConfigs();
            
            Map<String, Object> result = Map.of(
                "categories", configs.getOrDefault("categories", "[\"证件类\",\"电子设备\",\"生活用品\",\"学习用品\",\"服装配饰\",\"其他\"]"),
                "locations", configs.getOrDefault("locations", "[\"教学楼\",\"图书馆\",\"食堂\",\"宿舍\",\"体育馆\",\"实验室\",\"校园道路\",\"其他\"]"),
                "systemStatus", Map.of(
                    "server", "online",
                    "database", "online", 
                    "storage", "75%",
                    "api", "online"
                )
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return Result.error("获取配置失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/system/settings")
    public Result updateSystemSettings(@RequestBody Map<String, String> request,
                                     @RequestHeader("Authorization") String token) {
        try {
            boolean success = systemConfigService.batchUpdateConfigs(request);
            if (success) {
                return Result.success("设置更新成功");
            } else {
                return Result.error("设置更新失败");
            }
        } catch (Exception e) {
            log.error("更新系统设置失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats(@RequestHeader("Authorization") String token) {
        try {
            // 获取总用户数
            int totalUsers = userService.getUserCount(null, StatusConstants.USER_ENABLED);
            
            // 获取失物招领总数
            int totalItems = itemService.getItemCount(null, null, null);
            
            // 获取已找回物品数
            int recoveredItems = itemService.getItemCount(null, "claimed", null);
            
            // 计算找回率
            double recoveryRate = totalItems > 0 ? Math.round((double) recoveredItems / totalItems * 100) : 0;
            
            // 获取待审核信息数量
            int pendingReviews = itemService.getItemCount(null, StatusConstants.ITEM_STATUS_PENDING, null);
            
            Map<String, Object> dashboardData = Map.of(
                "totalUsers", totalUsers,
                "totalItems", totalItems,
                "recoveryRate", recoveryRate,
                "pendingReviews", pendingReviews,
                "todayReviewed", 12,
                "avgTime", 1.2,
                "weeklyReviewed", 45,
                "accuracy", 92.3
            );
            
            return Result.success(dashboardData);
        } catch (Exception e) {
            log.error("获取仪表板统计失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取管理员仪表板最新动态
     */
    @GetMapping("/dashboard/activities")
    public Result<List<Map<String, Object>>> getDashboardActivities(@RequestHeader("Authorization") String token) {
        try {
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // 1. 获取待审核的失物信息
            int pendingLostItems = itemService.getItemCount("lost", StatusConstants.ITEM_STATUS_PENDING, null);
            if (pendingLostItems > 0) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", 1);
                activity.put("type", "review");
                activity.put("icon", "📋");
                activity.put("title", "失物信息待审核");
                activity.put("description", "有 " + pendingLostItems + " 条失物信息等待审核");
                activity.put("time", "刚刚");
                activities.add(activity);
            }
            
            // 2. 获取待审核的招领信息
            int pendingFoundItems = itemService.getItemCount("found", StatusConstants.ITEM_STATUS_PENDING, null);
            if (pendingFoundItems > 0) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", 2);
                activity.put("type", "review");
                activity.put("icon", "📋");
                activity.put("title", "招领信息待审核");
                activity.put("description", "有 " + pendingFoundItems + " 条招领信息等待审核");
                activity.put("time", "刚刚");
                activities.add(activity);
            }
            
            // 3. 获取已找回的物品
            int recoveredItems = itemService.getItemCount(null, "claimed", null);
            if (recoveredItems > 0) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", 3);
                activity.put("type", "success");
                activity.put("icon", "🎉");
                activity.put("title", "物品找回成功");
                activity.put("description", "已有 " + recoveredItems + " 件物品成功找回");
                activity.put("time", "5分钟前");
                activities.add(activity);
            }
            
            // 4. 获取新注册用户数
            // 这里简化处理，实际应该查询最近注册的用户数
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", 4);
            activity.put("type", "user");
            activity.put("icon", "👤");
            activity.put("title", "新用户注册");
            activity.put("description", "今日有 3 位新用户注册");
            activity.put("time", "1小时前");
            activities.add(activity);
            
            return Result.success(activities);
        } catch (Exception e) {
            log.error("获取仪表板最新动态失败", e);
            return Result.error("获取最新动态失败：" + e.getMessage());
        }
    }
    
    // ========== 审核员管理功能 ==========
    
    /**
     * 获取审核员列表
     */
    @GetMapping("/reviewers")
    public Result<Map<String, Object>> getReviewers(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        
        try {
            var page = userService.getReviewerPage(current, pageSize, keyword, status);
            
            Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取审核员列表失败", e);
            return Result.error("获取审核员列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加审核员
     */
    @PostMapping("/reviewers")
    public Result<Map<String, Object>> addReviewer(
            @RequestBody Map<String, Object> reviewerData,
            @RequestHeader("Authorization") String token) {
        
        try {
            String username = (String) reviewerData.get("username");
            String name = (String) reviewerData.get("name");
            String email = (String) reviewerData.get("email");
            String phone = (String) reviewerData.get("phone");
            String password = (String) reviewerData.get("password");
            
            User reviewer = userService.createReviewer(username, name, email, phone, password);
            
            Map<String, Object> result = Map.of(
                "id", reviewer.getId(),
                "name", reviewer.getRealName(),
                "username", reviewer.getUsername(),
                "email", reviewer.getEmail(),
                "phone", reviewer.getPhone(),
                "status", reviewer.getStatus(),
                "joinTime", reviewer.getCreatedAt().toLocalDate().toString()
            );
            
            return Result.success("添加成功", result);
        } catch (Exception e) {
            log.error("添加审核员失败", e);
            return Result.error("添加审核员失败：" + e.getMessage());
        }
    }
    
    /**
     * 编辑审核员
     */
    @PutMapping("/reviewers/{id}")
    public Result<Map<String, Object>> updateReviewer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewerData,
            @RequestHeader("Authorization") String token) {
        
        try {
            String name = (String) reviewerData.get("name");
            String email = (String) reviewerData.get("email");
            String phone = (String) reviewerData.get("phone");
            String avatar = (String) reviewerData.get("avatar");
            
            User reviewer = userService.updateReviewer(id, name, email, phone, avatar);
            
            Map<String, Object> result = Map.of(
                "id", reviewer.getId(),
                "name", reviewer.getRealName(),
                "email", reviewer.getEmail()
            );
            
            return Result.success("编辑成功", result);
        } catch (Exception e) {
            log.error("编辑审核员失败", e);
            return Result.error("编辑审核员失败：" + e.getMessage());
        }
    }
    
    /**
     * 启用/禁用审核员
     */
    @PutMapping("/reviewers/{id}/status")
    public Result<Map<String, Object>> updateReviewerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData,
            @RequestHeader("Authorization") String token) {
        
        try {
            String status = statusData.get("status");
            
            userService.updateUserStatus(id, Integer.parseInt(status));
            
            Map<String, Object> result = Map.of(
                "id", id,
                "status", status
            );
            
            return Result.success("操作成功", result);
        } catch (Exception e) {
            log.error("更新审核员状态失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 查看审核员详情
     */
    @GetMapping("/reviewers/{id}")
    public Result<Map<String, Object>> getReviewerDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> reviewerDetail = userService.getReviewerDetail(id);
            
            return Result.success(reviewerDetail);
        } catch (Exception e) {
            log.error("获取审核员详情失败", e);
            return Result.error("获取审核员详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 导出用户数据
     */
    @GetMapping("/users/export")
    public Result<String> exportUsers(
            @RequestParam(required = false, defaultValue = "xlsx") String format,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        
        try {
            String filePath = userService.exportUsers(format, status);
            
            return Result.success("导出成功", filePath);
        } catch (Exception e) {
            log.error("导出用户数据失败", e);
            return Result.error("导出失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量操作用户
     */
    @PutMapping("/users/batch-status")
    public Result<Map<String, Object>> batchUpdateUserStatus(
            @RequestBody Map<String, Object> batchData,
            @RequestHeader("Authorization") String token) {
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) batchData.get("userIds");
            String status = (String) batchData.get("status");
            
            int affectedCount = userService.batchUpdateUserStatus(userIds, status);
            
            Map<String, Object> result = Map.of("affectedCount", affectedCount);
            
            return Result.success("操作成功", result);
        } catch (Exception e) {
            log.error("批量操作用户失败", e);
            return Result.error("批量操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除用户
     */
    @DeleteMapping("/users/batch-delete")
    public Result<Map<String, Object>> batchDeleteUsers(
            @RequestBody Map<String, Object> batchData,
            @RequestHeader("Authorization") String token) {
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) batchData.get("userIds");
            
            int affectedCount = userService.batchDeleteUsers(userIds);
            
            Map<String, Object> result = Map.of("affectedCount", affectedCount);
            
            return Result.success("删除成功", result);
        } catch (Exception e) {
            log.error("批量删除用户失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 重置用户密码
     */
    @PutMapping("/users/{id}/reset-password")
    public Result<Map<String, Object>> resetUserPassword(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String tempPassword = userService.resetUserPassword(id);
            
            Map<String, Object> result = Map.of("tempPassword", tempPassword);
            
            return Result.success("密码重置成功", result);
        } catch (Exception e) {
            log.error("重置用户密码失败", e);
            return Result.error("重置密码失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取失物招领趋势数据
     */
    @GetMapping("/statistics/trend")
    public Result<Map<String, Object>> getAdminTrendData(
            @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> trendData = statisticsService.getTrendStatistics(null, null, null);
            return Result.success(trendData);
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return Result.error("获取趋势数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取高频丢失物品数据
     */
    @GetMapping("/statistics/categories")
    public Result<Map<String, Object>> getAdminCategoryData(
            @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> categoryData = statisticsService.getCategoryStatistics();
            return Result.success(categoryData);
        } catch (Exception e) {
            log.error("获取类别数据失败", e);
            return Result.error("获取类别数据失败：" + e.getMessage());
        }
    }
}