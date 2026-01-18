package com.campus.errand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.ErrandTask;
import com.campus.errand.entity.User;
import com.campus.errand.service.TaskService;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 综合搜索
     * @param keyword 搜索关键词
     * @param type 搜索类型：task, user, all
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    @GetMapping
    public Map<String, Object> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "搜索关键词不能为空");
                response.put("data", null);
                return response;
            }
            
            Map<String, Object> searchResults = new HashMap<>();
            
            // 根据类型执行不同的搜索
            if ("task".equals(type) || "all".equals(type)) {
                // 搜索任务
                Page<ErrandTask> taskPage = taskService.getTaskList(page, pageSize, "pending", null, keyword);
                searchResults.put("tasks", taskPage.getRecords());
                searchResults.put("taskCount", taskPage.getTotal());
            }
            
            if ("user".equals(type) || "all".equals(type)) {
                // 搜索用户
                Page<User> userPage = userService.searchUsers(keyword, page, pageSize);
                searchResults.put("users", userPage.getRecords());
                searchResults.put("userCount", userPage.getTotal());
            }
            
            // 这里可以添加地点搜索
            // 暂时返回空的地点结果
            if ("location".equals(type) || "all".equals(type)) {
                searchResults.put("locations", null);
                searchResults.put("locationCount", 0);
            }
            
            response.put("code", 200);
            response.put("msg", "搜索成功");
            response.put("data", searchResults);
        } catch (Exception e) {
            log.error("搜索异常", e);
            response.put("code", 500);
            response.put("msg", "搜索失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
    
    /**
     * 获取热门搜索词
     * @return 热门搜索词列表
     */
    @GetMapping("/hot")
    public Map<String, Object> getHotSearches() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 这里可以从数据库或缓存中获取热门搜索词
            // 暂时返回固定的热门搜索词
            String[] hotSearches = {
                "快递代取",
                "外卖代送",
                "奶茶配送",
                "文件打印",
                "超市购物",
                "图书馆",
                "食堂",
                "校医院"
            };
            
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", hotSearches);
        } catch (Exception e) {
            log.error("获取热门搜索词异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}