package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserReview;
import com.campus.errand.entity.UserStatistics;
import com.campus.errand.entity.ErrandTask;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.mapper.UserReviewMapper;
import com.campus.errand.mapper.UserStatisticsMapper;
import com.campus.errand.mapper.ErrandTaskMapper;
import com.campus.errand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.List;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserReviewMapper userReviewMapper;

    @Autowired
    private UserStatisticsMapper userStatisticsMapper;

    @Autowired
    private ErrandTaskMapper errandTaskMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        // 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.warn("用户不存在: {}", username);
            return null;
        }

        // 验证密码
        System.out.println("用户输入密码: " + password);
        System.out.println("数据库存储密码: " + user.getPassword());
        System.out.println("密码长度: " + user.getPassword().length());
        
        // 临时允许明文密码验证（仅用于测试）
        if (!passwordEncoder.matches(password, user.getPassword()) && !"123456".equals(user.getPassword())) {
            log.warn("密码错误: {}", username);
            return null;
        }
        
        // 如果是明文密码，更新为BCrypt格式
        if ("123456".equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            userMapper.updateById(user);
            log.info("密码已更新为BCrypt格式: {}", username);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("用户状态异常: {}, 状态: {}", username, user.getStatus());
            return null;
        }

        return user;
    }

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        if (checkUsernameExists(user.getUsername())) {
            log.warn("用户名已存在: {}", user.getUsername());
            return null;
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().isEmpty() && checkPhoneExists(user.getPhone())) {
            log.warn("手机号已存在: {}", user.getPhone());
            return null;
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }
        // 设置默认头像
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
        }
        user.setStatus(1);
        user.setRegisterTime(new Date());

        // 保存用户
        int result = userMapper.insert(user);
        if (result > 0) {
            log.info("用户注册成功: {}", user.getUsername());
            return user;
        } else {
            log.error("用户注册失败: {}", user.getUsername());
            return null;
        }
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        // 确保用户有头像
        if (user != null && (user.getAvatar() == null || user.getAvatar().isEmpty())) {
            user.setAvatar("/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        // 确保用户有头像
        if (user != null && (user.getAvatar() == null || user.getAvatar().isEmpty())) {
            user.setAvatar("/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        int result = userMapper.updateById(user);
        if (result > 0) {
            return userMapper.selectById(user.getId());
        }
        return null;
    }

    @Override
    public boolean saveUserReview(UserReview review) {
        try {
            int result = userReviewMapper.insert(review);
            return result > 0;
        } catch (Exception e) {
            log.error("保存评价失败", e);
            return false;
        }
    }

    @Override
    public List<UserReview> getReceivedReviews(Long userId) {
        try {
            LambdaQueryWrapper<UserReview> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserReview::getRevieweeId, userId);
            queryWrapper.orderByDesc(UserReview::getCreateTime);
            return userReviewMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取收到的评价失败", e);
            return null;
        }
    }

    @Override
    public List<UserReview> getGivenReviews(Long userId) {
        try {
            LambdaQueryWrapper<UserReview> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserReview::getReviewerId, userId);
            queryWrapper.orderByDesc(UserReview::getCreateTime);
            return userReviewMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取发出的评价失败", e);
            return null;
        }
    }

    @Override
    public UserStatistics getUserStatistics(Long userId) {
        try {
            LambdaQueryWrapper<UserStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserStatistics::getUserId, userId);
            return userStatisticsMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return null;
        }
    }

    @Override
    public int calculateGoodRate(Long userId) {
        try {
            LambdaQueryWrapper<UserReview> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserReview::getRevieweeId, userId);
            List<UserReview> reviews = userReviewMapper.selectList(queryWrapper);

            if (reviews == null || reviews.isEmpty()) {
                return 100; // 默认100%好评率
            }

            int totalReviews = reviews.size();
            int goodReviews = 0;
            for (UserReview review : reviews) {
                if (review.getRating() >= 4.0) {
                    goodReviews++;
                }
            }

            return (goodReviews * 100) / totalReviews;
        } catch (Exception e) {
            log.error("计算好评率失败", e);
            return 100; // 异常时返回默认值
        }
    }

    @Override
    public int getPublishTaskCount(Long userId) {
        try {
            LambdaQueryWrapper<ErrandTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ErrandTask::getPublisherId, userId);
            return errandTaskMapper.selectCount(queryWrapper).intValue();
        } catch (Exception e) {
            log.error("获取发布订单数失败", e);
            return 0; // 异常时返回0
        }
    }
    
    @Override
    public boolean isTaskEvaluated(Long userId, Long taskId) {
        try {
            LambdaQueryWrapper<UserReview> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserReview::getReviewerId, userId);
            queryWrapper.eq(UserReview::getTaskId, taskId);
            Long count = userReviewMapper.selectCount(queryWrapper);
            return count > 0;
        } catch (Exception e) {
            log.error("检查订单是否已评价失败", e);
            return false; // 异常时返回false
        }
    }
    
    @Override
    public List<User> getRecommendedRunners() {
        try {
            // 查询所有角色为runner的用户，返回所有跑腿员
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getRole, "runner");
            queryWrapper.orderByDesc(User::getRegisterTime); // 按注册时间倒序排列
            List<User> runners = userMapper.selectList(queryWrapper);
            
            // 确保每个跑腿员都有头像
            if (runners != null) {
                for (User runner : runners) {
                    if (runner.getAvatar() == null || runner.getAvatar().isEmpty()) {
                        runner.setAvatar("/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
                    }
                }
            }
            
            return runners;
        } catch (Exception e) {
            log.error("获取推荐跑腿员失败", e);
            return null; // 异常时返回null
        }
    }
    
    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> searchUsers(String keyword, int page, int pageSize) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> userPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
            
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            // 搜索条件：用户名、昵称、真实姓名、手机号包含关键词
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword)
                );
            }
            
            // 按创建时间倒序排序
            queryWrapper.orderByDesc(User::getRegisterTime);
            
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result = userMapper.selectPage(userPage, queryWrapper);
            
            // 确保每个用户都有头像
            if (result != null && result.getRecords() != null) {
                for (User user : result.getRecords()) {
                    if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                        user.setAvatar("/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(); // 异常时返回空页
        }
    }
    
    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getAllUsers(int page, int pageSize, String role) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> userPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
            
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            // 角色过滤
            if (role != null && !role.isEmpty()) {
                queryWrapper.eq(User::getRole, role);
            }
            
            // 按创建时间倒序排序
            queryWrapper.orderByDesc(User::getRegisterTime);
            
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result = userMapper.selectPage(userPage, queryWrapper);
            
            // 确保每个用户都有头像
            if (result != null && result.getRecords() != null) {
                for (User user : result.getRecords()) {
                    if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                        user.setAvatar("/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg");
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(); // 异常时返回空页
        }
    }
    
    @Override
    public boolean updateUserStatus(Long userId, int status) {
        try {
            User user = new User();
            user.setId(userId);
            user.setStatus(status);
            
            int result = userMapper.updateById(user);
            log.info("更新用户状态成功 - 用户ID: {}, 状态: {}", userId, status);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateUserRole(Long userId, String role) {
        try {
            User user = new User();
            user.setId(userId);
            user.setRole(role);
            
            int result = userMapper.updateById(user);
            log.info("更新用户角色成功 - 用户ID: {}, 角色: {}", userId, role);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户角色失败", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        try {
            int result = userMapper.deleteById(userId);
            log.info("删除用户成功 - 用户ID: {}", userId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return false;
        }
    }
    
    @Override
    public long getUserCount(String role) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            // 角色过滤
            if (role != null && !role.isEmpty()) {
                queryWrapper.eq(User::getRole, role);
            }
            
            return userMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户总数失败", e);
            return 0;
        }
    }
}
