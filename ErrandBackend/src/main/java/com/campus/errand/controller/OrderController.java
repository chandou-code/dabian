package com.campus.errand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.ErrandTask;
import com.campus.errand.service.TaskService;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Map<String, Object> getOrderDetail(@PathVariable Long orderId, @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 这里我们假设订单ID就是任务ID
            ErrandTask task = taskService.getTaskById(orderId);
            if (task != null) {
                // 构建订单详情响应
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", task.getId());
                orderData.put("taskId", task.getId());
                orderData.put("taskNo", task.getTaskNo());
                orderData.put("orderNo", task.getTaskNo()); // 订单编号
                orderData.put("type", task.getType());
                orderData.put("title", task.getTitle());
                orderData.put("description", task.getDescription());
                orderData.put("price", task.getPrice());
                orderData.put("status", task.getStatus());
                orderData.put("publishTime", task.getPublishTime());
                orderData.put("createdAt", task.getPublishTime()); // 下单时间
                orderData.put("expectedTime", task.getExpectedTime());
                
                // 地址信息
                orderData.put("pickupAddress", task.getPickupAddress() + (task.getPickupDetail() != null ? " " + task.getPickupDetail() : ""));
                orderData.put("deliveryAddress", task.getDeliveryAddress() + (task.getDeliveryDetail() != null ? " " + task.getDeliveryDetail() : ""));
                orderData.put("pickupLatitude", task.getPickupLat());
                orderData.put("pickupLongitude", task.getPickupLng());
                orderData.put("deliveryLatitude", task.getDeliveryLat());
                orderData.put("deliveryLongitude", task.getDeliveryLng());
                orderData.put("latitude", task.getPickupLat()); // 地图中心点
                orderData.put("longitude", task.getPickupLng()); // 地图中心点
                
                // 计算距离和预计时间（模拟数据，实际项目中应该使用地图API计算）
                double distance = 0.5; // 默认0.5公里
                if (task.getPickupLat() != null && task.getPickupLng() != null && task.getDeliveryLat() != null && task.getDeliveryLng() != null) {
                    // 简单的距离计算（实际项目中应该使用更精确的算法）
                    double latDiff = Math.abs(task.getPickupLat() - task.getDeliveryLat());
                    double lngDiff = Math.abs(task.getPickupLng() - task.getDeliveryLng());
                    distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff) * 111.32; // 转换为公里
                    distance = Math.round(distance * 10) / 10.0; // 保留一位小数
                }
                orderData.put("distance", distance);
                orderData.put("estimatedTime", "约" + (int)(distance * 20) + "分钟"); // 假设每公里需要20分钟
                
                // 状态时间
                Date statusTime = null;
                switch (task.getStatus()) {
                    case "pending":
                        statusTime = task.getPublishTime();
                        break;
                    case "accepted":
                        statusTime = task.getAcceptTime();
                        break;
                    case "delivering":
                        statusTime = task.getStartTime();
                        break;
                    case "completed":
                        statusTime = task.getCompleteTime();
                        break;
                    case "cancelled":
                        statusTime = task.getCancelTime();
                        break;
                    default:
                        statusTime = task.getPublishTime();
                }
                orderData.put("statusTime", statusTime);
                
                // 其他信息
                orderData.put("remark", task.getRemark());
                orderData.put("images", task.getImages() != null ? task.getImages().split(",") : null); // 图片数组
                orderData.put("phone", task.getContactPhone()); // 联系电话
                
                // 发布者信息
                orderData.put("publisherId", task.getPublisherId());
                orderData.put("runnerId", task.getRunnerId());
                
                // 构建发布者信息
                Map<String, Object> publisher = new HashMap<>();
                com.campus.errand.entity.User publisherUser = userService.getUserById(task.getPublisherId());
                if (publisherUser != null) {
                    publisher.put("id", publisherUser.getId());
                    publisher.put("nickname", publisherUser.getNickname() != null ? publisherUser.getNickname() : publisherUser.getRealName() != null ? publisherUser.getRealName() : "发布者");
                    publisher.put("avatar", publisherUser.getAvatar() != null ? publisherUser.getAvatar() : "/static/default-avatar.png");
                } else {
                    publisher.put("id", task.getPublisherId());
                    publisher.put("nickname", "发布者");
                    publisher.put("avatar", "/static/default-avatar.png");
                }
                orderData.put("publisher", publisher);
                
                // 构建跑腿员信息
                if (task.getRunnerId() != null) {
                    // 获取真实的跑腿员信息
                    com.campus.errand.entity.User runnerUser = userService.getUserById(task.getRunnerId());
                    if (runnerUser != null) {
                        Map<String, Object> runner = new HashMap<>();
                        runner.put("id", runnerUser.getId());
                        runner.put("name", runnerUser.getNickname() != null ? runnerUser.getNickname() : runnerUser.getRealName() != null ? runnerUser.getRealName() : "跑腿员");
                        runner.put("avatar", runnerUser.getAvatar() != null ? runnerUser.getAvatar() : "/static/default-avatar.png");
                        
                        // 获取跑腿员的真实评分数据
                        int publishCount = userService.getPublishTaskCount(runnerUser.getId());
                        int goodRate = userService.calculateGoodRate(runnerUser.getId());
                        
                        runner.put("rating", goodRate / 20.0); // 将0-100的好评率转换为0-5的评分
                        runner.put("orderCount", publishCount);
                        runner.put("phone", runnerUser.getPhone()); // 添加电话信息，方便前端调用
                        
                        orderData.put("runner", runner);
                    }
                }
                
                // 检查订单是否已被评价
                boolean evaluated = false;
                if (currentUserId != null) {
                    evaluated = userService.isTaskEvaluated(currentUserId, task.getId());
                }
                orderData.put("evaluated", evaluated);
                
                response.put("code", 200);
                response.put("msg", "获取成功");
                response.put("data", orderData);
            } else {
                log.warn("订单不存在: {}", orderId);
                response.put("code", 404);
                response.put("msg", "订单不存在");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("获取订单详情异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public Map<String, Object> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "publisher") String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }
            
            Page<ErrandTask> taskPage;
            if ("runner".equals(role)) {
                // 获取跑腿员接的订单
                taskPage = taskService.getRunnerTasks(userId, page, pageSize, status);
            } else {
                // 获取用户发布的订单
                taskPage = taskService.getUserTasks(userId, page, pageSize, status);
            }
            
            // 构建订单列表响应
            Map<String, Object> data = new HashMap<>();
            
            // 为每个订单添加评价状态
            List<Map<String, Object>> orderList = new ArrayList<>();
            for (ErrandTask task : taskPage.getRecords()) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", task.getId());
                orderMap.put("taskNo", task.getTaskNo());
                orderMap.put("type", task.getType());
                orderMap.put("title", task.getTitle());
                orderMap.put("description", task.getDescription());
                orderMap.put("publisherId", task.getPublisherId());
                orderMap.put("runnerId", task.getRunnerId());
                orderMap.put("pickupAddress", task.getPickupAddress());
                orderMap.put("pickupDetail", task.getPickupDetail());
                orderMap.put("pickupLat", task.getPickupLat());
                orderMap.put("pickupLng", task.getPickupLng());
                orderMap.put("deliveryAddress", task.getDeliveryAddress());
                orderMap.put("deliveryDetail", task.getDeliveryDetail());
                orderMap.put("deliveryLat", task.getDeliveryLat());
                orderMap.put("deliveryLng", task.getDeliveryLng());
                orderMap.put("expectedTime", task.getExpectedTime());
                orderMap.put("price", task.getPrice());
                orderMap.put("contactPhone", task.getContactPhone());
                orderMap.put("remark", task.getRemark());
                orderMap.put("images", task.getImages());
                orderMap.put("status", task.getStatus());
                orderMap.put("publishTime", task.getPublishTime());
                orderMap.put("acceptTime", task.getAcceptTime());
                orderMap.put("startTime", task.getStartTime());
                orderMap.put("completeTime", task.getCompleteTime());
                orderMap.put("cancelTime", task.getCancelTime());
                orderMap.put("cancelReason", task.getCancelReason());
                
                // 检查订单是否已被评价
                boolean evaluated = userService.isTaskEvaluated(userId, task.getId());
                orderMap.put("evaluated", evaluated);
                
                orderList.add(orderMap);
            }
            
            data.put("list", orderList);
            data.put("total", taskPage.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", data);
        } catch (Exception e) {
            log.error("获取订单列表异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public Map<String, Object> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestBody Map<String, Object> statusData,
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = (String) statusData.get("status");
            // 这里我们复用任务状态更新接口
            boolean result = taskService.updateTaskStatus(orderId, status, userId);
            if (result) {
                response.put("code", 200);
                response.put("msg", "状态更新成功");
                response.put("data", null);
            } else {
                response.put("code", 400);
                response.put("msg", "状态更新失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("更新订单状态异常", e);
            response.put("code", 500);
            response.put("msg", "更新失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getOrderStats(
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }
            
            // 使用TaskService获取用户的订单统计数据
            Map<String, Integer> stats = taskService.getUserOrderStats(userId);
            
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", stats);
        } catch (Exception e) {
            log.error("获取订单统计异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}
