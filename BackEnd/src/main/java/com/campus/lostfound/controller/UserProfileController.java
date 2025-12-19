package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;
import com.campus.lostfound.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户个人资料控制器
 */
@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserProfileController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/profile")
    public Result<User> getProfile(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            User user = userService.getUserById(userId);
            if (user != null) {
                user.setPassword(null); // 不返回密码
                return Result.success("获取成功", user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestHeader("Authorization") String authorization,
                                   @RequestBody Map<String, String> profileData) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            User userInfo = new User();
            userInfo.setRealName(profileData.get("realName"));
            userInfo.setEmail(profileData.get("email"));
            userInfo.setPhone(profileData.get("phone"));
            userInfo.setAvatar(profileData.get("avatar"));
            userInfo.setCollege(profileData.get("college"));
            userInfo.setGrade(profileData.get("grade"));
            
            boolean success = userService.updateUserInfo(userId, userInfo);
            if (success) {
                User updatedUser = userService.getUserById(userId);
                updatedUser.setPassword(null);
                return Result.success("更新成功", updatedUser);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/password")
    public Result updatePassword(@RequestHeader("Authorization") String authorization,
                              @RequestBody Map<String, String> passwordData) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            // 验证新密码
            if (!newPassword.equals(confirmPassword)) {
                return Result.error("新密码与确认密码不一致");
            }
            
            boolean success = userService.updatePassword(userId, currentPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功", null);
            } else {
                return Result.error("当前密码错误");
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改失败：" + e.getMessage());
        }
    }
}