package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.User;
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
}