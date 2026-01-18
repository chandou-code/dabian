package com.campus.errand.controller;

import com.campus.errand.entity.User;
import com.campus.errand.service.TaskService;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;

    /**
     * 获取用户列表（管理员）
     */
    @GetMapping("/users/list")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限访问");
                response.put("data", null);
                return response;
            }

            // 获取用户列表
            var userPage = userService.getAllUsers(page, pageSize, role);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("list", userPage.getRecords());
            data.put("total", userPage.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", userPage.getPages());

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", data);
            return response;
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 更新用户状态（管理员）
     */
    @PutMapping("/users/{userId}/status")
    public Map<String, Object> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> requestData,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限操作");
                response.put("data", null);
                return response;
            }

            // 更新用户状态
            int status = requestData.getOrDefault("status", 1);
            boolean result = userService.updateUserStatus(userId, status);

            if (result) {
                response.put("code", 200);
                response.put("msg", "更新成功");
                response.put("data", null);
            } else {
                response.put("code", 500);
                response.put("msg", "更新失败");
                response.put("data", null);
            }
            return response;
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            response.put("code", 500);
            response.put("msg", "更新失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 更新用户角色（管理员）
     */
    @PutMapping("/users/{userId}/role")
    public Map<String, Object> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> requestData,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限操作");
                response.put("data", null);
                return response;
            }

            // 更新用户角色
            String role = requestData.getOrDefault("role", "user");
            boolean result = userService.updateUserRole(userId, role);

            if (result) {
                response.put("code", 200);
                response.put("msg", "更新成功");
                response.put("data", null);
            } else {
                response.put("code", 500);
                response.put("msg", "更新失败");
                response.put("data", null);
            }
            return response;
        } catch (Exception e) {
            log.error("更新用户角色失败", e);
            response.put("code", 500);
            response.put("msg", "更新失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/users/{userId}")
    public Map<String, Object> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限操作");
                response.put("data", null);
                return response;
            }

            // 删除用户
            boolean result = userService.deleteUser(userId);

            if (result) {
                response.put("code", 200);
                response.put("msg", "删除成功");
                response.put("data", null);
            } else {
                response.put("code", 500);
                response.put("msg", "删除失败");
                response.put("data", null);
            }
            return response;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            response.put("code", 500);
            response.put("msg", "删除失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 获取用户统计（管理员）
     */
    @GetMapping("/users/stats")
    public Map<String, Object> getUserStats(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限访问");
                response.put("data", null);
                return response;
            }

            // 获取各角色用户数量
            long totalCount = userService.getUserCount(null);
            long userCount = userService.getUserCount("user");
            long runnerCount = userService.getUserCount("runner");
            long adminCount = userService.getUserCount("admin");

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("total", totalCount);
            data.put("users", userCount);
            data.put("runners", runnerCount);
            data.put("admins", adminCount);

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", data);
            return response;
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 搜索用户（管理员）
     */
    @GetMapping("/users/search")
    public Map<String, Object> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限访问");
                response.put("data", null);
                return response;
            }

            // 搜索用户
            var userPage = userService.searchUsers(keyword, page, pageSize);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("list", userPage.getRecords());
            data.put("total", userPage.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", userPage.getPages());

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", data);
            return response;
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            response.put("code", 500);
            response.put("msg", "搜索失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 获取任务列表（管理员）
     */
    @GetMapping("/tasks/list")
    public Map<String, Object> getTaskList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限访问");
                response.put("data", null);
                return response;
            }

            // 获取任务列表
            var taskPage = taskService.getTaskList(page, pageSize, status, type, keyword);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("list", taskPage.getRecords());
            data.put("total", taskPage.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", taskPage.getPages());

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", data);
            return response;
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 获取任务统计（管理员）
     */
    @GetMapping("/tasks/stats")
    public Map<String, Object> getTaskStats(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限访问");
                response.put("data", null);
                return response;
            }

            // 获取各状态任务数量
            long totalCount = taskService.getTaskList(1, 1, null, null, null).getTotal();
            long pendingCount = taskService.getTaskList(1, 1, "pending", null, null).getTotal();
            long inProgressCount = taskService.getTaskList(1, 1, "in_progress", null, null).getTotal();
            long completedCount = taskService.getTaskList(1, 1, "completed", null, null).getTotal();

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("total", totalCount);
            data.put("pending", pendingCount);
            data.put("in_progress", inProgressCount);
            data.put("completed", completedCount);

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", data);
            return response;
        } catch (Exception e) {
            log.error("获取任务统计失败", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }



    /**
     * 取消任务（管理员）
     */
    @PostMapping("/tasks/{id}/cancel")
    public Map<String, Object> cancelTask(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限操作");
                response.put("data", null);
                return response;
            }

            // 取消任务
            boolean result = taskService.cancelTask(id, null);

            if (result) {
                response.put("code", 200);
                response.put("msg", "取消成功");
                response.put("data", null);
            } else {
                response.put("code", 500);
                response.put("msg", "取消失败");
                response.put("data", null);
            }
            return response;
        } catch (Exception e) {
            log.error("取消任务失败", e);
            response.put("code", 500);
            response.put("msg", "取消失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    /**
     * 删除任务（管理员）
     */
    @DeleteMapping("/tasks/{id}")
    public Map<String, Object> deleteTask(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户权限
            String currentRole = (String) request.getAttribute("currentRole");
            if (!"admin".equals(currentRole)) {
                response.put("code", 403);
                response.put("msg", "无权限操作");
                response.put("data", null);
                return response;
            }

            // 删除任务
            boolean result = taskService.deleteTask(id, null);

            if (result) {
                response.put("code", 200);
                response.put("msg", "删除成功");
                response.put("data", null);
            } else {
                response.put("code", 500);
                response.put("msg", "删除失败");
                response.put("data", null);
            }
            return response;
        } catch (Exception e) {
            log.error("删除任务失败", e);
            response.put("code", 500);
            response.put("msg", "删除失败：" + e.getMessage());
            response.put("data", null);
            return response;
        }
    }
}
