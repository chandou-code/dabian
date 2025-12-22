package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.MatchMapper;
import com.campus.lostfound.mapper.NotificationMapper;
import com.campus.lostfound.mapper.ReviewMapper;
import com.campus.lostfound.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/test")
@CrossOrigin
public class DatabaseTestController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private MatchMapper matchMapper;
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @GetMapping("/db-connection")
    public Result<Map<String, Object>> testDatabaseConnection() {
        try {
            // 测试数据库连接
            List<User> users = userMapper.selectList(null);
            
            Map<String, Object> result = Map.of(
                "status", "success",
                "message", "数据库连接正常",
                "userCount", users.size(),
                "users", users.stream().limit(3).map(user -> {
                    Map<String, Object> userMap = Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "realName", user.getRealName(),
                        "role", user.getRole()
                    );
                    return userMap;
                }).toList()
            );
            
            return Result.success("测试成功", result);
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return Result.error("数据库连接失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/user/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userMapper.selectById(id);
            if (user != null) {
                user.setPassword(null);
                return Result.success("查询成功", user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/create-user")
    public Result<User> createUser(@RequestBody User user) {
        try {
            // 简单创建测试用户
            userMapper.insert(user);
            
            user.setPassword(null);
            return Result.success("创建成功", user);
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/clear-lost-items")
    public Result<String> clearLostItems() {
        try {
            log.info("开始清空失物数据");
            
            // 使用BaseMapper的delete方法直接删除失物数据
            // MyBatis Plus会自动处理关联关系的级联删除（如果配置了的话）
            int deletedCount = itemMapper.deleteByMap(Map.of("type", "lost"));
            
            log.info("成功清空 {} 条失物数据", deletedCount);
            return Result.success("失物数据已成功清空，共删除 " + deletedCount + " 条记录");
        } catch (Exception e) {
            log.error("清空失物数据失败", e);
            return Result.error("清空失物数据失败：" + e.getMessage());
        }
    }
}