package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物品控制器
 */
@Slf4j

@RestController
@RequestMapping("/items")
@CrossOrigin
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 从Authorization头中提取用户ID
     */
    private Long extractUserIdFromToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            log.warn("无效的Authorization头: {}", authorization);
            return null;
        }
        
        String token = authorization.substring(7);
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("从token中提取用户ID失败", e);
            return null;
        }
    }
    
    // ========== 符合API文档的URL映射 ==========
    
    @GetMapping("/test-simple")
    public Result<String> testSimple() {
        return Result.success("ItemController工作正常");
    }
    
    @GetMapping("/lost-items")
    public Result<Map<String, Object>> getItems(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long submitterId) {
        
        try {
            log.info("获取失物列表请求: current={}, pageSize={}", current, pageSize);
            var page = itemService.getItemPage(current, pageSize, "lost", category, status, keyword, location, submitterId);
            
            Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords().stream().map(item -> {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("itemName", item.getItemName());
                    itemMap.put("category", item.getCategory());
                    itemMap.put("lostTime", item.getEventTime());
                    itemMap.put("lostLocation", item.getLocation());
                    itemMap.put("description", item.getDescription());
                    itemMap.put("images", getImagesArray(item.getImages()));
                    itemMap.put("status", item.getStatus());
                    itemMap.put("userId", item.getSubmitterId());
                    itemMap.put("userName", item.getSubmitterName());
                    itemMap.put("created_at", item.getCreatedAt());
                    return itemMap;
                }).toList()
            );
            
            log.info("获取失物列表成功: total={}", page.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取失物列表失败", e);
            return Result.error("获取失物列表失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/lost-items")
    public Result<Item> publishLostItemNew(@RequestBody Item item, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("发布失物信息请求: {}", item.getItemName());
            
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            Item savedItem = itemService.publishLostItem(item, userId);
            if (savedItem != null) {
                log.info("发布失物信息成功: id={}, userId={}", savedItem.getId(), userId);
                return Result.success("发布成功", savedItem);
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            log.error("发布失物信息失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/found-items")
    public Result<Map<String, Object>> getFoundItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateRange) {
        
        try {
            // 解析日期范围
            String startDate = null;
            String endDate = null;
            if (dateRange != null && !dateRange.trim().isEmpty()) {
                String[] dates = dateRange.split(",");
                if (dates.length == 2) {
                    startDate = dates[0].trim();
                    endDate = dates[1].trim();
                }
            }
            
            var pageResult = itemService.getItemPage(page, pageSize, "found", category, status, keyword, null, null);
            
            Map<String, Object> result = Map.of(
                "total", pageResult.getTotal(),
                "list", pageResult.getRecords().stream().map(item -> {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("itemName", item.getItemName());
                    itemMap.put("category", item.getCategory());
                    itemMap.put("foundTime", item.getEventTime());
                    itemMap.put("foundLocation", item.getLocation());
                    itemMap.put("description", item.getDescription());
                    itemMap.put("images", getImagesArray(item.getImages()));
                    itemMap.put("status", item.getStatus());
                    itemMap.put("userId", item.getSubmitterId());
                    itemMap.put("userName", item.getSubmitterName());
                    itemMap.put("created_at", item.getCreatedAt());
                    return itemMap;
                }).toList()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取招领列表失败", e);
            return Result.error("获取招领列表失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/found-items")
    public Result<Item> publishFoundItemNew(@RequestBody Item item, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("发布招领信息请求: {}", item.getItemName());
            
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            Item savedItem = itemService.publishFoundItem(item, userId);
            if (savedItem != null) {
                log.info("发布招领信息成功: id={}, userId={}", savedItem.getId(), userId);
                return Result.success("发布成功", savedItem);
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            log.error("发布招领信息失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }
    
    // ========== 原有的功能保持兼容性 ==========
    
    @GetMapping({"/lost-item/{id}", "/found-item/{id}"})
    public Result<Map<String, Object>> getItemDetail(@PathVariable Long id) {
        try {
            Item item = itemService.getItemDetail(id);
            if (item == null) {
                return Result.error("物品不存在");
            }
            
            Map<String, Object> itemDetail = new java.util.HashMap<>();
            itemDetail.put("id", item.getId());
            itemDetail.put("itemName", item.getItemName());
            itemDetail.put("category", item.getCategory());
            
            if ("lost".equals(item.getType())) {
                itemDetail.put("lostTime", item.getEventTime());
                itemDetail.put("lostLocation", item.getLocation());
            } else {
                itemDetail.put("foundTime", item.getEventTime());
                itemDetail.put("foundLocation", item.getLocation());
            }
            
            itemDetail.put("description", item.getDescription());
            itemDetail.put("contact", item.getContact());
            itemDetail.put("images", getImagesArray(item.getImages()));
            itemDetail.put("status", item.getStatus());
            itemDetail.put("userId", item.getSubmitterId());
            itemDetail.put("userName", item.getSubmitterName());
            itemDetail.put("created_at", item.getCreatedAt());
            itemDetail.put("updated_at", item.getUpdatedAt());
            
            return Result.success(itemDetail);
        } catch (Exception e) {
            log.error("获取物品详情失败", e);
            return Result.error("获取物品详情失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/lost")
    public Result<Item> publishLostItem(@RequestBody Item item, @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            Item savedItem = itemService.publishLostItem(item, userId);
            if (savedItem != null) {
                return Result.success("发布成功", savedItem);
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            log.error("发布失物信息失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/found")
    public Result<Item> publishFoundItem(@RequestBody Item item, @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            Item savedItem = itemService.publishFoundItem(item, userId);
            if (savedItem != null) {
                return Result.success("发布成功", savedItem);
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            log.error("发布招领信息失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public Result<Map<String, Object>> searchItems(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String timeRange,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            var page = itemService.searchItems(current, pageSize, q, type, category, timeRange);
            
            Map<String, Object> result = Map.of(
                "results", page.getRecords(),
                "total", page.getTotal()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索物品失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/stats/user")
    public Result<Map<String, Object>> getUserStats(@RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            Map<String, Object> stats = itemService.getUserStatistics(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/found")
    public Result markItemFound(@PathVariable Long id,
                               @RequestBody Map<String, Object> request) {
        try {
            Long matchedItemId = null;
            if (request.containsKey("matchedItemId")) {
                matchedItemId = Long.valueOf(request.get("matchedItemId").toString());
            }
            
            boolean success = itemService.markItemFound(id, matchedItemId);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            log.error("标记物品已找到失败", e);
            return Result.error("标记失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/recovered")
    public Result markItemRecovered(@PathVariable Long id) {
        try {
            boolean success = itemService.markItemRecovered(id);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            log.error("标记物品已领回失败", e);
            return Result.error("标记失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/items/{id}")
    public Result<Item> updateItem(@PathVariable Long id, 
                                  @RequestBody Item item, 
                                  @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            // 验证物品是否存在且属于当前用户
            Item existingItem = itemService.getById(id);
            if (existingItem == null) {
                return Result.error("物品不存在");
            }
            
            if (!existingItem.getSubmitterId().equals(userId)) {
                return Result.error("无权限修改此物品");
            }
            
            // 更新物品信息
            item.setId(id);
            item.setSubmitterId(existingItem.getSubmitterId());
            item.setSubmitterName(existingItem.getSubmitterName());
            item.setType(existingItem.getType());
            item.setStatus(existingItem.getStatus());
            item.setUpdatedAt(java.time.LocalDateTime.now());
            
            boolean success = itemService.updateById(item);
            if (success) {
                return Result.success("更新成功", item);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新物品信息失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/items/{id}")
    public Result deleteItem(@PathVariable Long id, @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            // 验证物品是否存在且属于当前用户
            Item existingItem = itemService.getById(id);
            if (existingItem == null) {
                return Result.error("物品不存在");
            }
            
            if (!existingItem.getSubmitterId().equals(userId)) {
                return Result.error("无权限删除此物品");
            }
            
            boolean success = itemService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除物品失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/activities")
    public Result<List<Item>> getRecentActivities(@RequestHeader("Authorization") String authorization,
                                                 @RequestParam(defaultValue = "10") int limit) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            List<Item> activities = itemService.getUserRecentActivities(userId, limit);
            return Result.success(activities);
        } catch (Exception e) {
            log.error("获取最近活动失败", e);
            return Result.error("获取最近活动失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/matches")
    public Result<List<Item>> getRecommendedMatches(@RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            List<Item> matches = itemService.getRecommendedMatches(userId);
            return Result.success(matches);
        } catch (Exception e) {
            log.error("获取推荐匹配失败", e);
            return Result.error("获取推荐匹配失败：" + e.getMessage());
        }
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 获取图片数组
     */
    private String[] getImagesArray(String images) {
        if (images == null || images.trim().isEmpty()) {
            return new String[0];
        }
        
        try {
            if (images.startsWith("[") && images.endsWith("]")) {
                String cleanImages = images.substring(1, images.length() - 1);
                return cleanImages.split(",");
            } else {
                return new String[]{images};
            }
        } catch (Exception e) {
            log.error("解析图片URL失败", e);
            return new String[0];
        }
    }
}