package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    User login(String username, String password, String loginIp);
    
    /**
     * 用户注册
     */
    User register(User user);
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
    
    /**
     * 分页查询用户
     */
    IPage<User> getUserPage(int current, int size, String keyword, String role, Integer status);
    
    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 重置用户密码
     */
    String resetPassword(Long userId);
    
    /**
     * 批量操作用户
     */
    boolean batchOperateUsers(Long[] userIds, String action);
    
    /**
     * 获取用户统计
     */
    int getUserCount(String role, Integer status);
    
    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email);
    
    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 更新用户信息
     */
    boolean updateUserInfo(Long userId, User userInfo);
    
    /**
     * 修改密码
     */
    boolean updatePassword(Long userId, String currentPassword, String newPassword);
    
    /**
     * 获取审核员分页列表
     */
    IPage<User> getReviewerPage(int current, int pageSize, String keyword, String status);
    
    /**
     * 创建审核员
     */
    User createReviewer(String username, String name, String email, String phone, String password);
    
    /**
     * 更新审核员信息
     */
    User updateReviewer(Long id, String name, String email, String phone, String avatar);
    
    /**
     * 获取审核员详情
     */
    java.util.Map<String, Object> getReviewerDetail(Long id);
    
    /**
     * 导出用户数据
     */
    String exportUsers(String format, Integer status);
    
    /**
     * 批量更新用户状态
     */
    int batchUpdateUserStatus(java.util.List<Long> userIds, String status);
    
    /**
     * 批量删除用户
     */
    int batchDeleteUsers(java.util.List<Long> userIds);
    
    /**
     * 重置用户密码
     */
    String resetUserPassword(Long userId);
    
    /**
     * 获取总用户数
     */
    int getTotalUserCount();
}