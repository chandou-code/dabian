package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final UserMapper userMapper;
    
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    

    
    @Override
    public User login(String username, String password, String loginIp) {
        try {
            log.info("开始验证用户登录 - 用户名: {}", username);
            
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username)
                   .eq(User::getStatus, 1);
            
            User user = getOne(wrapper);
            if (user == null) {
                log.warn("用户不存在或已禁用: {}", username);
                return null;
            }
            
            String encryptedPassword = encryptPassword(password);
            log.info("密码验证 - 输入密码加密后: {}, 数据库密码: {}", encryptedPassword, user.getPassword());
            
            if (encryptedPassword.equals(user.getPassword())) {
                log.info("密码验证成功 - 用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
                // 更新登录时间和IP
                user.setUpdatedAt(LocalDateTime.now());
                updateById(user);
                return user;
            } else {
                log.warn("密码验证失败 - 用户: {}, 加密密码不匹配", username);
                return null;
            }
        } catch (Exception e) {
            log.error("用户登录异常", e);
            return null;
        }
    }
    
    @Override
    public User register(User user) {
        try {
            // 检查用户名是否已存在
            if (checkUsernameExists(user.getUsername())) {
                return null;
            }
            
            // 检查邮箱是否已存在
            if (user.getEmail() != null && checkEmailExists(user.getEmail())) {
                return null;
            }
            
            // 加密密码
            user.setPassword(encryptPassword(user.getPassword()));
            user.setRole("user");
            user.setStatus(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            save(user);
            user.setPassword(null); // 返回时不返回密码
            return user;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return null;
        }
    }
    
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
    
    @Override
    public IPage<User> getUserPage(int current, int size, String keyword, String role, Integer status) {
        Page<User> page = new Page<>(current, size);
        return baseMapper.selectUserPage(page, keyword, role, status);
    }
    
    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }
    
    @Override
    public String resetPassword(Long userId) {
        try {
            User user = getById(userId);
            if (user == null) {
                return null;
            }
            
            // 生成临时密码
            String tempPassword = "123456";
            String encodedPassword = encryptPassword(tempPassword);
            
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            updateById(user);
            
            return tempPassword;
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return null;
        }
    }
    
    @Override
    public boolean batchOperateUsers(Long[] userIds, String action) {
        try {
            if ("enable".equals(action)) {
                return baseMapper.batchUpdateStatus(Arrays.asList(userIds), 1) > 0;
            } else if ("disable".equals(action)) {
                return baseMapper.batchUpdateStatus(Arrays.asList(userIds), 0) > 0;
            }
            return false;
        } catch (Exception e) {
            log.error("批量操作用户失败", e);
            return false;
        }
    }
    
    @Override
    public int getUserCount(String role, Integer status) {
        return Math.toIntExact(baseMapper.getUserCount(null, status));
    }
    
    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return count(wrapper) > 0;
    }
    
    @Override
    public boolean checkEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return count(wrapper) > 0;
    }
    
    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }
    
    @Override
    public boolean updateUserInfo(Long userId, User userInfo) {
        try {
            User user = new User();
            user.setId(userId);
            user.setRealName(userInfo.getRealName());
            user.setEmail(userInfo.getEmail());
            user.setPhone(userInfo.getPhone());
            user.setAvatar(userInfo.getAvatar());
            user.setCollege(userInfo.getCollege());
            user.setGrade(userInfo.getGrade());
            user.setRole(userInfo.getRole());
            user.setUpdatedAt(LocalDateTime.now());
            
            return updateById(user);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return false;
        }
    }
    
    /**
     * MD5加密密码
     */
    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("密码加密失败", e);
            return password;
        }
    }

    @Override
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        try {
            User user = getById(userId);
            if (user == null) {
                return false;
            }
            
            // 验证当前密码
            if (!encryptPassword(currentPassword).equals(user.getPassword())) {
                return false;
            }
            
            // 更新密码
            user.setPassword(encryptPassword(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            
            return updateById(user);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return false;
        }
    }
    
    @Override
    public IPage<User> getReviewerPage(int current, int pageSize, String keyword, String status) {
        Page<User> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(User::getRole, "reviewer");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                              .or().like(User::getRealName, keyword)
                              .or().like(User::getEmail, keyword));
        }
        
        if (status != null && !status.trim().isEmpty()) {
            if ("active".equals(status)) {
                wrapper.eq(User::getStatus, 1);
            } else if ("inactive".equals(status)) {
                wrapper.eq(User::getStatus, 0);
            }
        }
        
        return page(page, wrapper);
    }
    
    @Override
    public User createReviewer(String username, String name, String email, String phone, String password) {
        // 检查用户名是否已存在
        if (checkUsernameExists(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (checkEmailExists(email)) {
            throw new RuntimeException("邮箱已存在");
        }
        
        User reviewer = new User();
        reviewer.setUsername(username);
        reviewer.setRealName(name);
        reviewer.setEmail(email);
        reviewer.setPhone(phone);
        reviewer.setPassword(encryptPassword(password));
        reviewer.setRole("reviewer");
        reviewer.setStatus(1);
        reviewer.setCreatedAt(LocalDateTime.now());
        reviewer.setUpdatedAt(LocalDateTime.now());
        
        save(reviewer);
        return reviewer;
    }
    
    @Override
    public User updateReviewer(Long id, String name, String email, String phone, String avatar) {
        User reviewer = getById(id);
        if (reviewer == null || !"reviewer".equals(reviewer.getRole())) {
            throw new RuntimeException("审核员不存在");
        }
        
        if (name != null) reviewer.setRealName(name);
        if (email != null) reviewer.setEmail(email);
        if (phone != null) reviewer.setPhone(phone);
        if (avatar != null) reviewer.setAvatar(avatar);
        
        reviewer.setUpdatedAt(LocalDateTime.now());
        
        updateById(reviewer);
        return reviewer;
    }
    
    @Override
    public java.util.Map<String, Object> getReviewerDetail(Long id) {
        User reviewer = getById(id);
        if (reviewer == null || !"reviewer".equals(reviewer.getRole())) {
            throw new RuntimeException("审核员不存在");
        }
        
        java.util.Map<String, Object> detail = new java.util.HashMap<>();
        detail.put("id", reviewer.getId());
        detail.put("name", reviewer.getRealName());
        detail.put("username", reviewer.getUsername());
        detail.put("email", reviewer.getEmail());
        detail.put("phone", reviewer.getPhone());
        detail.put("status", reviewer.getStatus() == 1 ? "active" : "inactive");
        detail.put("avatar", reviewer.getAvatar());
        detail.put("joinTime", reviewer.getCreatedAt().toLocalDate().toString());
        
        // 获取审核统计（这里简化处理）
        detail.put("totalReviewed", 150);
        detail.put("approved", 135);
        detail.put("rejected", 15);
        detail.put("approvalRate", 90.0);
        
        // 获取审核历史（这里简化处理）
        java.util.List<java.util.Map<String, Object>> reviewHistory = new java.util.ArrayList<>();
        java.util.Map<String, Object> history = new java.util.HashMap<>();
        history.put("itemId", 101);
        history.put("itemName", "书包");
        history.put("type", "lost");
        history.put("result", "approved");
        history.put("reviewTime", "2024-01-01 16:30:00");
        reviewHistory.add(history);
        detail.put("reviewHistory", reviewHistory);
        
        return detail;
    }
    
    @Override
    public String exportUsers(String format, Integer status) {
        // 简化处理，实际应该实现真实的导出逻辑
        String filePath = "/tmp/users_export." + format;
        // TODO: 实现真实的导出逻辑
        return filePath;
    }
    
    @Override
    public int batchUpdateUserStatus(java.util.List<Long> userIds, String status) {
        int newStatus = "active".equals(status) ? 1 : 0;
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getId, userIds);
        
        User updateUser = new User();
        updateUser.setStatus(newStatus);
        updateUser.setUpdatedAt(LocalDateTime.now());
        
        return userMapper.update(updateUser, wrapper);
    }
    
    @Override
    public int batchDeleteUsers(java.util.List<Long> userIds) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getId, userIds);
        
        return userMapper.delete(wrapper);
    }
    
    @Override
    public String resetUserPassword(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        String tempPassword = "123456";
        user.setPassword(encryptPassword(tempPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        updateById(user);
        return tempPassword;
    }
    
    @Override
    public int getTotalUserCount() {
        return (int) count();
    }
}