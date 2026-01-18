package com.campus.errand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.ErrandTask;
import com.campus.errand.service.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 发布任务
     */
    @PostMapping("/publish")
    public Map<String, Object> publishTask(@RequestBody Map<String, Object> taskData, @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ErrandTask task = new ErrandTask();
            task.setPublisherId(userId);
            
            // 基本信息
            task.setType((String) taskData.get("type"));
            task.setTitle((String) taskData.get("title"));
            task.setDescription((String) taskData.get("description"));
            
            // 地址信息
            task.setPickupAddress((String) taskData.get("pickupAddress"));
            task.setPickupDetail((String) taskData.get("pickupDetail"));
            task.setDeliveryAddress((String) taskData.get("deliveryAddress"));
            task.setDeliveryDetail((String) taskData.get("deliveryDetail"));
            
            // 时间信息
            String expectedTimeStr = (String) taskData.get("expectedTime");
            if (expectedTimeStr != null && !expectedTimeStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    task.setExpectedTime(sdf.parse(expectedTimeStr));
                } catch (Exception e) {
                    log.warn("解析期望时间失败: {}", expectedTimeStr);
                }
            }
            
            // 价格信息
            Object priceObj = taskData.get("price");
            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    task.setPrice(((Number) priceObj).doubleValue());
                } else if (priceObj instanceof String) {
                    try {
                        task.setPrice(Double.parseDouble((String) priceObj));
                    } catch (Exception e) {
                        log.warn("解析价格失败: {}", priceObj);
                    }
                }
            }
            
            // 联系方式
            task.setContactPhone((String) taskData.get("phone"));
            
            // 备注
            task.setRemark((String) taskData.get("remark"));
            
            // 图片信息 - 转换为有效的JSON字符串
            Object imagesObj = taskData.get("images");
            if (imagesObj != null) {
                if (imagesObj instanceof List) {
                    // 使用Jackson将List转换为JSON字符串
                    try {
                        task.setImages(objectMapper.writeValueAsString(imagesObj));
                    } catch (JsonProcessingException e) {
                        log.error("转换图片列表为JSON失败", e);
                        // 如果转换失败，使用空JSON数组
                        task.setImages("[]");
                    }
                } else if (imagesObj instanceof String) {
                    String imagesStr = (String) imagesObj;
                    // 如果已经是JSON字符串，直接使用
                    task.setImages(imagesStr);
                } else {
                    // 其他类型，转换为JSON
                    try {
                        task.setImages(objectMapper.writeValueAsString(imagesObj));
                    } catch (JsonProcessingException e) {
                        log.error("转换图片数据为JSON失败", e);
                        task.setImages("[]");
                    }
                }
            } else {
                // 图片为空，使用空JSON数组
                task.setImages("[]");
            }
            
            // 设置默认状态
            task.setStatus("pending");
            task.setPublishTime(new Date());
            
            ErrandTask publishedTask = taskService.publishTask(task);

            if (publishedTask != null) {
                log.info("任务发布成功: {}, 发布人: {}", publishedTask.getId(), userId);
                response.put("code", 200);
                response.put("msg", "发布成功");
                response.put("data", publishedTask);
            } else {
                log.warn("任务发布失败: 发布人: {}", userId);
                response.put("code", 400);
                response.put("msg", "发布失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("发布任务异常", e);
            response.put("code", 500);
            response.put("msg", "发布失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    public Map<String, Object> getTaskList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<ErrandTask> taskPage = taskService.getTaskList(page, pageSize, status, type, keyword);

            Map<String, Object> data = new HashMap<>();
            data.put("total", taskPage.getTotal());
            data.put("list", taskPage.getRecords());
            data.put("current", taskPage.getCurrent());
            data.put("size", taskPage.getSize());

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", data);
        } catch (Exception e) {
            log.error("获取任务列表异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取用户发布的任务列表
     */
    @GetMapping("/user-tasks")
    public Map<String, Object> getUserTasks(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<ErrandTask> taskPage = taskService.getUserTasks(userId, page, pageSize, status);

            Map<String, Object> data = new HashMap<>();
            data.put("total", taskPage.getTotal());
            data.put("list", taskPage.getRecords());
            data.put("current", taskPage.getCurrent());
            data.put("size", taskPage.getSize());

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", data);
        } catch (Exception e) {
            log.error("获取用户任务列表异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}/detail")
    public Map<String, Object> getTaskDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ErrandTask task = taskService.getTaskById(id);
            if (task != null) {
                response.put("code", 200);
                response.put("msg", "获取成功");
                response.put("data", task);
            } else {
                log.warn("任务不存在: {}", id);
                response.put("code", 404);
                response.put("msg", "任务不存在");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("获取任务详情异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 更新任务
     */
    @PutMapping("/update")
    public Map<String, Object> updateTask(@RequestBody ErrandTask task, @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            task.setPublisherId(userId);
            ErrandTask updatedTask = taskService.updateTask(task);

            if (updatedTask != null) {
                log.info("任务更新成功: {}, 更新人: {}", updatedTask.getId(), userId);
                response.put("code", 200);
                response.put("msg", "更新成功");
                response.put("data", updatedTask);
            } else {
                log.warn("任务更新失败: {}, 更新人: {}", task.getId(), userId);
                response.put("code", 400);
                response.put("msg", "更新失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("更新任务异常", e);
            response.put("code", 500);
            response.put("msg", "更新失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteTask(@PathVariable Long id, @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = taskService.deleteTask(id, userId);
            if (result) {
                log.info("任务删除成功: {}, 删除人: {}", id, userId);
                response.put("code", 200);
                response.put("msg", "删除成功");
                response.put("data", null);
            } else {
                log.warn("任务删除失败: {}, 删除人: {}", id, userId);
                response.put("code", 400);
                response.put("msg", "删除失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("删除任务异常", e);
            response.put("code", 500);
            response.put("msg", "删除失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 接受任务
     */
    @PostMapping("/{id}/accept")
    public Map<String, Object> acceptTask(@PathVariable Long id, @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = taskService.acceptTask(id, userId);
            if (result) {
                log.info("任务接受成功: {}, 接单人: {}", id, userId);
                response.put("code", 200);
                response.put("msg", "接受成功");
                response.put("data", null);
            } else {
                log.warn("任务接受失败: {}, 接单人: {}", id, userId);
                response.put("code", 400);
                response.put("msg", "接受失败：不能接受自己发布的任务或任务状态不允许");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("接受任务异常", e);
            response.put("code", 500);
            response.put("msg", "接受失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 完成任务
     */
    @PostMapping("/{id}/complete")
    public Map<String, Object> completeTask(@PathVariable Long id, @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = taskService.completeTask(id, userId);
            if (result) {
                log.info("任务完成成功: {}, 操作人: {}", id, userId);
                response.put("code", 200);
                response.put("msg", "完成成功");
                response.put("data", null);
            } else {
                log.warn("任务完成失败: {}, 操作人: {}", id, userId);
                response.put("code", 400);
                response.put("msg", "完成失败：任务状态不允许或无权限");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("完成任务异常", e);
            response.put("code", 500);
            response.put("msg", "完成失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 取消任务
     */
    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancelTask(@PathVariable Long id, @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = taskService.cancelTask(id, userId);
            if (result) {
                log.info("任务取消成功: {}, 操作人: {}", id, userId);
                response.put("code", 200);
                response.put("msg", "取消成功");
                response.put("data", null);
            } else {
                log.warn("任务取消失败: {}, 操作人: {}", id, userId);
                response.put("code", 400);
                response.put("msg", "取消失败：任务状态不允许或无权限");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("取消任务异常", e);
            response.put("code", 500);
            response.put("msg", "取消失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 更新任务状态
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = taskService.updateTaskStatus(id, status, userId);
            if (result) {
                log.info("任务状态更新成功: {}, 状态: {}, 操作人: {}", id, status, userId);
                response.put("code", 200);
                response.put("msg", "状态更新成功");
                response.put("data", null);
            } else {
                log.warn("任务状态更新失败: {}, 状态: {}, 操作人: {}", id, status, userId);
                response.put("code", 400);
                response.put("msg", "状态更新失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("更新任务状态异常", e);
            response.put("code", 500);
            response.put("msg", "状态更新失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}
