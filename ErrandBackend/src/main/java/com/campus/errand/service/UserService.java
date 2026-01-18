package com.campus.errand.service;

import com.campus.errand.entity.User;
import com.campus.errand.entity.UserReview;
import com.campus.errand.entity.UserStatistics;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User login(String username, String password);

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册后的用户信息
     */
    User register(User user);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean checkPhoneExists(String phone);

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 保存用户评价
     * @param review 评价信息
     * @return 是否保存成功
     */
    boolean saveUserReview(UserReview review);

    /**
     * 获取用户收到的评价列表
     * @param userId 用户ID
     * @return 评价列表
     */
    List<UserReview> getReceivedReviews(Long userId);

    /**
     * 获取用户发出的评价列表
     * @param userId 用户ID
     * @return 评价列表
     */
    List<UserReview> getGivenReviews(Long userId);
    
    /**
     * 获取用户统计信息
     * @param userId 用户ID
     * @return 用户统计信息
     */
    UserStatistics getUserStatistics(Long userId);
    
    /**
     * 计算用户的好评率
     * @param userId 用户ID
     * @return 好评率（0-100）
     */
    int calculateGoodRate(Long userId);
    
    /**
     * 获取用户发布的订单数
     * @param userId 用户ID
     * @return 发布订单数
     */
    int getPublishTaskCount(Long userId);
    
    /**
     * 检查订单是否已被评价
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 是否已评价
     */
    boolean isTaskEvaluated(Long userId, Long taskId);
    
    /**
     * 获取推荐跑腿员
     * @return 推荐跑腿员列表
     */
    List<User> getRecommendedRunners();
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> searchUsers(String keyword, int page, int pageSize);
    
    /**
     * 获取所有用户列表
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param role 用户角色过滤
     * @return 用户列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getAllUsers(int page, int pageSize, String role);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态：0-禁用 1-启用
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long userId, int status);
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色：user/runner/admin
     * @return 是否更新成功
     */
    boolean updateUserRole(Long userId, String role);
    
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long userId);
    
    /**
     * 获取用户总数
     * @param role 用户角色过滤
     * @return 用户总数
     */
    long getUserCount(String role);
}
