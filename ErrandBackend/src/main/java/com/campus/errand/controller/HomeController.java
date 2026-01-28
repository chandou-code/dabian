package com.campus.errand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.ErrandTask;
import com.campus.errand.entity.User;
import com.campus.errand.service.TaskService;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页控制器
 */
@Slf4j
@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    /**
     * 获取轮播图数据
     */
    @GetMapping("/banners")
    public Map<String, Object> getBanners() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 返回轮播图数据
            List<Map<String, String>> banners = new ArrayList<>();
            // 使用静态资源目录中的轮播图
            banners.add(Map.of("image", "/static/banners/064c1423-da21-4672-bd41-c0ddaf71c4c6.png", "link", "/#/"));
            banners.add(Map.of("image", "/static/banners/2e55097651da6296102c49461ac8e0d1.png", "link", "/#/"));
            banners.add(Map.of("image", "/static/banners/d717101d-ee99-4b15-95b6-0d1ca8b227ac.png", "link", "/#/"));

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", banners);
        } catch (Exception e) {
            log.error("获取轮播图异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getUserStats(@RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                // 如果未登录，返回默认数据
                Map<String, Integer> stats = new HashMap<>();
                stats.put("pendingTasks", 0);
                stats.put("inProgressTasks", 0);
                stats.put("completedTasks", 0);

                response.put("code", 200);
                response.put("msg", "获取成功");
                response.put("data", stats);
                return response;
            }

            // 获取用户发布的订单统计
            Map<String, Integer> userStats = taskService.getUserOrderStats(userId);

            // 构建响应数据
            Map<String, Integer> stats = new HashMap<>();
            stats.put("pendingTasks", userStats.getOrDefault("pending", 0));
            stats.put("inProgressTasks", userStats.getOrDefault("inProgress", 0));
            stats.put("completedTasks", userStats.getOrDefault("completed", 0));

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", stats);
        } catch (Exception e) {
            log.error("获取用户统计异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取推荐跑腿员
     */
    @GetMapping("/recommended-runners")
    public Map<String, Object> getRecommendedRunners() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取推荐的跑腿员（真实数据）
            List<User> runners = userService.getRecommendedRunners();
            
            // 构建响应数据
            List<Map<String, Object>> runnerList = new ArrayList<>();
            for (User runner : runners) {
                Map<String, Object> runnerMap = new HashMap<>();
                runnerMap.put("id", runner.getId());
                runnerMap.put("name", runner.getNickname() != null ? runner.getNickname() : runner.getRealName() != null ? runner.getRealName() : runner.getUsername());
                runnerMap.put("avatar", runner.getAvatar() != null ? runner.getAvatar() : "/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
                
                // 获取跑腿员的评分数据
                int goodRate = userService.calculateGoodRate(runner.getId());
                int orderCount = userService.getPublishTaskCount(runner.getId());
                
                runnerMap.put("rating", goodRate / 20.0); // 将0-100的好评率转换为0-5的评分
                runnerMap.put("orderCount", orderCount);
                
                runnerList.add(runnerMap);
            }

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", runnerList);
        } catch (Exception e) {
            log.error("获取推荐跑腿员异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取平台公告
     */
    @GetMapping("/notices")
    public Map<String, Object> getNotices() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 返回平台公告数据
            List<Map<String, Object>> notices = new ArrayList<>();
            notices.add(Map.of(
                    "id", 1,
                    "title", "校园跑腿平台上线公告",
                    "createdAt", "2026-01-15T10:00:00"
            ));
            notices.add(Map.of(
                    "id", 2,
                    "title", "安全使用指南",
                    "createdAt", "2026-01-14T15:30:00"
            ));
            notices.add(Map.of(
                    "id", 3,
                    "title", "跑腿员招募计划",
                    "createdAt", "2026-01-13T09:00:00"
            ));

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", notices);
        } catch (Exception e) {
            log.error("获取平台公告异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取首页所有数据
     */
    @GetMapping("/all")
    public Map<String, Object> getAllHomeData(@RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取轮播图数据
            List<Map<String, String>> banners = new ArrayList<>();
            banners.add(Map.of("image", "/static/banners/064c1423-da21-4672-bd41-c0ddaf71c4c6.png", "link", "/#/"));
            banners.add(Map.of("image", "/static/banners/2e55097651da6296102c49461ac8e0d1.png", "link", "/#/"));
            banners.add(Map.of("image", "/static/banners/d717101d-ee99-4b15-95b6-0d1ca8b227ac.png", "link", "/#/"));

            // 获取用户统计数据
            Map<String, Integer> stats = new HashMap<>();
            if (userId != null) {
                Map<String, Integer> userStats = taskService.getUserOrderStats(userId);
                stats.put("pendingTasks", userStats.getOrDefault("pending", 0));
                stats.put("inProgressTasks", userStats.getOrDefault("inProgress", 0));
                stats.put("completedTasks", userStats.getOrDefault("completed", 0));
            } else {
                stats.put("pendingTasks", 0);
                stats.put("inProgressTasks", 0);
                stats.put("completedTasks", 0);
            }

            // 获取附近任务（使用已有的任务列表API）
            Page<ErrandTask> taskPage = taskService.getTaskList(1, 5, null, null, null);
            List<Map<String, Object>> nearbyTasks = new ArrayList<>();
            for (ErrandTask task : taskPage.getRecords()) {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("type", task.getType());
                taskMap.put("title", task.getTitle());
                taskMap.put("price", task.getPrice());
                taskMap.put("pickupAddress", task.getPickupAddress());
                taskMap.put("deliveryAddress", task.getDeliveryAddress());
                taskMap.put("distance", 0.5); // 默认0.5公里
                taskMap.put("expectedTime", task.getExpectedTime());
                nearbyTasks.add(taskMap);
            }

            // 获取推荐跑腿员
            List<User> runners = userService.getRecommendedRunners();
            List<Map<String, Object>> recommendedRunners = new ArrayList<>();
            for (User runner : runners) {
                Map<String, Object> runnerMap = new HashMap<>();
                runnerMap.put("id", runner.getId());
                runnerMap.put("name", runner.getNickname() != null ? runner.getNickname() : runner.getRealName() != null ? runner.getRealName() : runner.getUsername());
                runnerMap.put("avatar", runner.getAvatar() != null ? runner.getAvatar() : "/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
                
                int goodRate = userService.calculateGoodRate(runner.getId());
                int orderCount = userService.getPublishTaskCount(runner.getId());
                
                runnerMap.put("rating", goodRate / 20.0);
                runnerMap.put("orderCount", orderCount);
                
                recommendedRunners.add(runnerMap);
            }

            // 获取平台公告
            List<Map<String, Object>> notices = new ArrayList<>();
            notices.add(Map.of(
                    "id", 1,
                    "title", "校园跑腿平台上线公告",
                    "createdAt", "2026-01-15T10:00:00"
            ));
            notices.add(Map.of(
                    "id", 2,
                    "title", "安全使用指南",
                    "createdAt", "2026-01-14T15:30:00"
            ));
            notices.add(Map.of(
                    "id", 3,
                    "title", "跑腿员招募计划",
                    "createdAt", "2026-01-13T09:00:00"
            ));

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("banners", banners);
            data.put("userStats", stats);
            data.put("nearbyTasks", nearbyTasks);
            data.put("recommendedRunners", recommendedRunners);
            data.put("notices", notices);

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", data);
        } catch (Exception e) {
            log.error("获取首页数据异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}
