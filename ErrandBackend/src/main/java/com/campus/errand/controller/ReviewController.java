package com.campus.errand.controller;

import com.campus.errand.entity.ErrandTask;
import com.campus.errand.entity.UserReview;
import com.campus.errand.service.TaskService;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    /**
     * 提交评价
     */
    @PostMapping("/{orderId}")
    public Map<String, Object> submitReview(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> reviewData,
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查用户是否已登录
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "未授权，请登录");
                response.put("data", null);
                return response;
            }
            
            // 处理orderId参数
            Long taskId = null;
            if (orderId != null && !"null".equals(orderId)) {
                try {
                    taskId = Long.parseLong(orderId);
                } catch (NumberFormatException e) {
                    log.warn("解析orderId失败: {}", orderId);
                }
            }

            // 尝试从请求体中获取taskId
            if (taskId == null) {
                Object taskIdObj = reviewData.get("taskId");
                if (taskIdObj != null) {
                    if (taskIdObj instanceof Number) {
                        taskId = ((Number) taskIdObj).longValue();
                    } else if (taskIdObj instanceof String) {
                        try {
                            taskId = Long.parseLong((String) taskIdObj);
                        } catch (NumberFormatException e) {
                            log.warn("解析任务ID失败: {}", taskIdObj);
                        }
                    }
                }
            }

            // 尝试从请求体中获取orderId
            if (taskId == null) {
                Object orderIdObj = reviewData.get("orderId");
                if (orderIdObj != null && !"null".equals(orderIdObj) && orderIdObj != null) {
                    if (orderIdObj instanceof Number) {
                        taskId = ((Number) orderIdObj).longValue();
                    } else if (orderIdObj instanceof String && !"null".equals(orderIdObj)) {
                        try {
                            taskId = Long.parseLong((String) orderIdObj);
                        } catch (NumberFormatException e) {
                            log.warn("解析orderId失败: {}", orderIdObj);
                        }
                    }
                }
            }

            if (taskId == null) {
                response.put("code", 400);
                response.put("msg", "任务ID不能为空");
                response.put("data", null);
                return response;
            }

            // 获取任务信息，用于获取跑腿员ID
            ErrandTask task = taskService.getTaskById(taskId);
            if (task == null) {
                response.put("code", 404);
                response.put("msg", "任务不存在");
                response.put("data", null);
                return response;
            }
            
            // 检查任务是否已经被接单
            if (task.getRunnerId() == null) {
                response.put("code", 400);
                response.put("msg", "任务尚未被接单，无法评价");
                response.put("data", null);
                return response;
            }

            // 创建评价对象
            UserReview review = new UserReview();
            review.setReviewerId(userId);
            review.setRevieweeId(task.getRunnerId());
            review.setTaskId(taskId);

            // 评价内容
            Double rating = null;
            Object ratingObj = reviewData.get("rating");
            if (ratingObj != null) {
                if (ratingObj instanceof Number) {
                    rating = ((Number) ratingObj).doubleValue();
                } else if (ratingObj instanceof String) {
                    try {
                        rating = Double.parseDouble((String) ratingObj);
                    } catch (NumberFormatException e) {
                        log.warn("解析评分失败: {}", ratingObj);
                    }
                }
            }
            review.setRating(rating);

            // 评价内容
            String comment = (String) reviewData.get("comment");
            review.setContent(comment);

            // 评价标签
            Object tagsObj = reviewData.get("tags");
            if (tagsObj != null) {
                if (tagsObj instanceof List) {
                    // 将List转换为JSON字符串
                    StringBuilder jsonBuilder = new StringBuilder("[");
                    List<?> tagsList = (List<?>) tagsObj;
                    for (int i = 0; i < tagsList.size(); i++) {
                        if (i > 0) {
                            jsonBuilder.append(",");
                        }
                        Object tag = tagsList.get(i);
                        if (tag instanceof String) {
                            jsonBuilder.append('"').append(tag).append('"');
                        } else {
                            jsonBuilder.append(tag);
                        }
                    }
                    jsonBuilder.append("]");
                    review.setTags(jsonBuilder.toString());
                } else if (tagsObj instanceof String) {
                    review.setTags((String) tagsObj);
                }
            }

            // 详细评分
            Object detailRatingsObj = reviewData.get("detailRatings");
            if (detailRatingsObj != null) {
                log.info("详细评分: {}", detailRatingsObj);
            }

            // 图片
            Object imagesObj = reviewData.get("images");
            if (imagesObj != null) {
                log.info("评价图片: {}", imagesObj);
            }

            // 是否匿名
            Boolean isAnonymous = false;
            Object isAnonymousObj = reviewData.get("isAnonymous");
            if (isAnonymousObj != null && isAnonymousObj instanceof Boolean) {
                isAnonymous = (Boolean) isAnonymousObj;
            }

            review.setCreateTime(new Date());

            // 保存评价
            boolean result = userService.saveUserReview(review);

            if (result) {
                log.info("评价提交成功: 任务ID={}, 用户ID={}", taskId, userId);
                response.put("code", 200);
                response.put("msg", "评价提交成功");
                response.put("data", review);
            } else {
                log.warn("评价提交失败: 任务ID={}, 用户ID={}", taskId, userId);
                response.put("code", 400);
                response.put("msg", "评价提交失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("提交评价异常", e);
            response.put("code", 500);
            response.put("msg", "评价提交失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取用户收到的评价列表
     */
    @GetMapping("/received")
    public Map<String, Object> getReceivedReviews(
            @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<UserReview> reviews = userService.getReceivedReviews(userId);
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", reviews);
        } catch (Exception e) {
            log.error("获取收到的评价异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取用户发出的评价列表
     */
    @GetMapping("/given")
    public Map<String, Object> getGivenReviews(
            @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<UserReview> reviews = userService.getGivenReviews(userId);
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", reviews);
        } catch (Exception e) {
            log.error("获取发出的评价异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}
