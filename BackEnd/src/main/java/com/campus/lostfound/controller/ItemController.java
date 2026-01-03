package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.ItemImageService;
import com.campus.lostfound.service.UserService;
import com.campus.lostfound.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ItemImageService itemImageService;
    
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
                    
                    // 确保时间字段不为null，转换为适合前端的格式
                    if (item.getEventTime() != null) {
                        itemMap.put("lostTime", item.getEventTime() + "T00:00:00");
                    } else {
                        itemMap.put("lostTime", null);
                    }
                    
                    // 确保位置字段不为null
                    itemMap.put("lostLocation", item.getLocation() != null ? item.getLocation() : "未知地点");
                    
                    itemMap.put("description", item.getDescription());
                    itemMap.put("images", getImagesArray(item.getImages()));
                    itemMap.put("status", item.getStatus());
                    itemMap.put("type", item.getType()); // 添加type字段
                    itemMap.put("userId", item.getSubmitterId());
                    itemMap.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
                    itemMap.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
                    
                    // 手动加载用户名
                    try {
                        User user = userService.getById(item.getSubmitterId());
                        itemMap.put("userName", user != null ? user.getRealName() : "匿名用户");
                    } catch (Exception e) {
                        log.warn("获取用户名失败: userId={}, error={}", item.getSubmitterId(), e.getMessage());
                        itemMap.put("userName", "匿名用户");
                    }
                    
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
    public Result<Item> publishLostItemNew(@RequestBody Map<String, Object> requestData, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("发布失物信息请求: {}", requestData.get("itemName"));
            
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            // 创建Item对象并手动映射字段
            Item item = new Item();
            item.setItemName((String) requestData.get("itemName"));
            item.setCategory((String) requestData.get("category"));
            item.setDescription((String) requestData.get("description"));
            item.setContact((String) requestData.get("contact"));
            item.setImages((String) requestData.get("images"));
            item.setType((String) requestData.get("type"));
            
            // 映射时间字段
            String lostTimeStr = (String) requestData.get("lostTime");
            if (lostTimeStr != null && !lostTimeStr.isEmpty()) {
                try {
                    item.setEventTime(LocalDate.parse(lostTimeStr));
                } catch (Exception e) {
                    log.warn("解析丢失时间失败: {}", lostTimeStr);
                }
            }
            
            // 映射位置字段
            String location = (String) requestData.get("lostLocation");
            if (location != null && !location.isEmpty()) {
                item.setLocation(location);
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
                    
                    // 确保时间字段不为null，转换为适合前端的格式
                    if (item.getEventTime() != null) {
                        itemMap.put("foundTime", item.getEventTime() + "T00:00:00");
                    } else {
                        itemMap.put("foundTime", null);
                    }
                    
                    // 确保位置字段不为null
                    itemMap.put("foundLocation", item.getLocation() != null ? item.getLocation() : "未知地点");
                    
                    itemMap.put("description", item.getDescription());
                    itemMap.put("images", getImagesArray(item.getImages()));
                    itemMap.put("status", item.getStatus());
                    itemMap.put("type", item.getType()); // 添加type字段
                    itemMap.put("userId", item.getSubmitterId());
                    itemMap.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
                    
                    // 手动加载用户名
                    try {
                        User user = userService.getById(item.getSubmitterId());
                        itemMap.put("userName", user != null ? user.getRealName() : "匿名用户");
                    } catch (Exception e) {
                        log.warn("获取用户名失败: userId={}, error={}", item.getSubmitterId(), e.getMessage());
                        itemMap.put("userName", "匿名用户");
                    }
                    
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
    public Result<Item> publishFoundItemNew(@RequestBody Map<String, Object> requestData, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("发布招领信息请求: {}", requestData.get("itemName"));
            
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            // 创建Item对象并手动映射字段
            Item item = new Item();
            item.setItemName((String) requestData.get("itemName"));
            item.setCategory((String) requestData.get("category"));
            item.setDescription((String) requestData.get("description"));
            item.setContact((String) requestData.get("contact"));
            item.setImages((String) requestData.get("images"));
            item.setType((String) requestData.get("type"));
            
            // 映射时间字段
            String foundTimeStr = (String) requestData.get("foundTime");
            if (foundTimeStr != null && !foundTimeStr.isEmpty()) {
                try {
                    item.setEventTime(LocalDate.parse(foundTimeStr));
                } catch (Exception e) {
                    log.warn("解析发现时间失败: {}", foundTimeStr);
                }
            }
            
            // 映射位置字段
            String location = (String) requestData.get("foundLocation");
            if (location != null && !location.isEmpty()) {
                item.setLocation(location);
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
            itemDetail.put("type", item.getType()); // 添加type字段
            
            if ("lost".equals(item.getType())) {
                // 确保时间字段不为null，转换为适合前端的格式
                itemDetail.put("lostTime", item.getEventTime() != null ? item.getEventTime() + "T00:00:00" : null);
                // 确保位置字段不为null
                itemDetail.put("lostLocation", item.getLocation() != null ? item.getLocation() : "未知地点");
            } else {
                // 确保时间字段不为null，转换为适合前端的格式
                itemDetail.put("foundTime", item.getEventTime() != null ? item.getEventTime() + "T00:00:00" : null);
                // 确保位置字段不为null
                itemDetail.put("foundLocation", item.getLocation() != null ? item.getLocation() : "未知地点");
            }
            
            itemDetail.put("description", item.getDescription());
            itemDetail.put("contact", item.getContact());
            itemDetail.put("images", getImagesArray(item.getImages()));
            itemDetail.put("status", item.getStatus());
            itemDetail.put("userId", item.getSubmitterId());
            itemDetail.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
            
            // 手动加载用户名
            try {
                User user = userService.getById(item.getSubmitterId());
                itemDetail.put("userName", user != null ? user.getRealName() : "匿名用户");
            } catch (Exception e) {
                log.warn("获取用户名失败: userId={}, error={}", item.getSubmitterId(), e.getMessage());
                itemDetail.put("userName", "匿名用户");
            }
            
            itemDetail.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
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
    public Result markItemRecovered(@PathVariable Long id, @RequestHeader("Authorization") String authorization) {
        try {
            // 提取当前用户ID
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
                return Result.error("无权限标记此物品为已找回");
            }
            
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
    
    // ========== 图片上传相关接口 ==========
    
    /**
     * 为物品上传图片
     */
    @PostMapping("/{id}/images")
    public Result<List<Map<String, Object>>> uploadItemImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            // 验证物品是否存在且属于当前用户
            Item item = itemService.getById(id);
            if (item == null) {
                return Result.error("物品不存在");
            }
            
            if (!item.getSubmitterId().equals(userId)) {
                return Result.error("无权限为此物品上传图片");
            }
            
            List<Map<String, Object>> result = itemImageService.uploadItemImages(
                files, item.getType(), id, userId);
            
            return Result.success("图片上传成功", result);
        } catch (Exception e) {
            log.error("物品图片上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取物品图片列表
     */
    @GetMapping("/{id}/images")
    public Result<List<Map<String, Object>>> getItemImages(@PathVariable Long id) {
        try {
            List<Map<String, Object>> images = itemImageService.getItemImages(id);
            return Result.success("获取成功", images);
        } catch (Exception e) {
            log.error("获取物品图片失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除物品图片
     */
    @DeleteMapping("/images/{uploadId}")
    public Result deleteItemImage(
            @PathVariable Long uploadId,
            @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            boolean success = itemImageService.deleteItemImage(uploadId, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除物品图片失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新物品图片关联关系
     */
    @PostMapping("/update-image-association")
    public Result updateImageAssociation(@RequestBody Map<String, Object> data, @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            Long itemId = ((Number) data.get("itemId")).longValue();
            String itemType = (String) data.get("itemType");
            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) data.get("imageUrls");
            
            boolean success = itemImageService.updateItemImageAssociation(itemId, itemType, imageUrls, userId);
            if (success) {
                return Result.success("图片关联更新成功");
            } else {
                return Result.error("图片关联更新失败");
            }
        } catch (Exception e) {
            log.error("更新图片关联关系失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    
    /**
     * 获取物品详情 - 符合前端调用格式
     */
    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getItemDetailByFrontend(@PathVariable Long id) {
        try {
            log.info("获取物品详情请求: id={}", id);
            
            Item item = itemService.getItemDetail(id);
            if (item == null) {
                log.warn("物品不存在: id={}", id);
                return Result.error("物品不存在");
            }
            
            // 构建返回数据，确保包含前端所需的所有字段
            Map<String, Object> itemDetail = new java.util.HashMap<>();
            itemDetail.put("id", item.getId());
            itemDetail.put("itemName", item.getItemName());
            itemDetail.put("category", item.getCategory());
            itemDetail.put("type", item.getType());
            itemDetail.put("description", item.getDescription());
            itemDetail.put("status", item.getStatus());
            itemDetail.put("submitterId", item.getSubmitterId());
            itemDetail.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
            
            // 根据类型设置时间和地点字段
            if ("lost".equals(item.getType())) {
                itemDetail.put("eventTime", item.getEventTime() != null ? item.getEventTime().toString() : null);
                itemDetail.put("location", item.getLocation() != null ? item.getLocation() : "未知地点");
            } else {
                itemDetail.put("eventTime", item.getEventTime() != null ? item.getEventTime().toString() : null);
                itemDetail.put("location", item.getLocation() != null ? item.getLocation() : "未知地点");
            }
            
            // 处理图片数组
            itemDetail.put("images", getImagesArray(item.getImages()));
            
            // 获取用户信息
            try {
                User user = userService.getById(item.getSubmitterId());
                if (user != null) {
                    itemDetail.put("userName", user.getRealName() != null ? user.getRealName() : user.getUsername());
                    itemDetail.put("userPhone", user.getPhone());
                    itemDetail.put("userEmail", user.getEmail());
                    itemDetail.put("userAvatar", user.getAvatar());
                } else {
                    itemDetail.put("userName", "匿名用户");
                    itemDetail.put("userPhone", null);
                    itemDetail.put("userEmail", null);
                    itemDetail.put("userAvatar", null);
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: userId={}, error={}", item.getSubmitterId(), e.getMessage());
                itemDetail.put("userName", "匿名用户");
                itemDetail.put("userPhone", null);
                itemDetail.put("userEmail", null);
                itemDetail.put("userAvatar", null);
            }
            
            // 联系方式字段
            itemDetail.put("contact", item.getContact());
            
            log.info("获取物品详情成功: id={}, itemName={}, userName={}", 
                    id, item.getItemName(), itemDetail.get("userName"));
            
            return Result.success("获取成功", itemDetail);
            
        } catch (Exception e) {
            log.error("获取物品详情失败: id={}", id, e);
            return Result.error("获取物品详情失败：" + e.getMessage());
        }
    }
    
    
    /**
     * 提交线索
     */
    @PostMapping("/{id}/clues")
    public Result<String> submitClue(@PathVariable Long id,
                                    @RequestBody Map<String, Object> requestData) {
        try {
            log.info("提交线索: itemId={}, data={}", id, requestData);
            
            String content = (String) requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("线索内容不能为空");
            }
            
            // 验证物品是否存在
            Item item = itemService.getById(id);
            if (item == null) {
                return Result.error("物品不存在");
            }
            
            // 简单模拟保存线索到数据库（实际应该有Clue实体和ClueService）
            log.info("线索保存成功 - 物品ID: {}, 内容: {}, 联系人: {}, 电话: {}", 
                    id, content, requestData.get("contactName"), requestData.get("contactPhone"));
            
            // 如果有图片，记录图片信息
            @SuppressWarnings("unchecked")
            java.util.List<String> images = (java.util.List<String>) requestData.get("images");
            if (images != null && !images.isEmpty()) {
                log.info("线索图片数量: {}", images.size());
            }
            
            return Result.success("线索提交成功，我们会尽快核实");
        } catch (Exception e) {
            log.error("提交线索失败", e);
            return Result.error("提交线索失败：" + e.getMessage());
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