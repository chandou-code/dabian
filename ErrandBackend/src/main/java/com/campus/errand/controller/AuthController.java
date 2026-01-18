package com.campus.errand.controller;

import com.campus.errand.entity.User;
import com.campus.errand.entity.UserReview;
import com.campus.errand.service.UserService;
import com.campus.errand.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        try {
            System.out.println("接收到登录请求");
            System.out.println("请求参数: " + loginData);
            System.out.println("请求方法: " + request.getMethod());
            System.out.println("请求路径: " + request.getRequestURI());
            System.out.println("请求头: " + request.getHeaderNames());

            String username = loginData.get("username");
            String password = loginData.get("password");

            log.info("登录请求 - 用户名: {}", username);

            User user = userService.login(username, password);

            if (user != null) {
                log.info("登录成功 - 用户ID: {}, 用户名: {}, 角色: {}", user.getId(), user.getUsername(), user.getRole());

                // 生成JWT Token
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                System.out.println("生成Token: " + token);

                // 构建响应数据
                Map<String, Object> response = new HashMap<>();
                Map<String, Object> userInfo = new HashMap<>();

                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("realName", user.getRealName());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("role", user.getRole());
                userInfo.put("email", user.getEmail());
                userInfo.put("phone", user.getPhone());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("college", user.getCollege());
                userInfo.put("grade", user.getGrade());

                response.put("user", userInfo);
                response.put("token", token);

                Map<String, Object> result = Map.of("code", 200, "msg", "登录成功", "data", response);
                System.out.println("登录响应: " + result);
                return result;
            } else {
                log.warn("登录失败 - 用户名或密码错误: {}", username);
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("msg", "用户名或密码错误");
                result.put("data", null);
                System.out.println("登录失败响应: " + result);
                return result;
            }
        } catch (Exception e) {
            log.error("登录异常", e);
            System.out.println("登录异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "登录失败：" + e.getMessage());
            result.put("data", null);
            System.out.println("登录异常响应: " + result);
            return result;
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData) {
        try {
            User user = new User();
            user.setUsername(registerData.get("username"));
            user.setPassword(registerData.get("password"));
            user.setEmail(registerData.get("email"));
            user.setPhone(registerData.get("phone"));
            user.setRealName(registerData.get("realName"));
            user.setNickname(registerData.get("nickname"));
            user.setCollege(registerData.get("college"));
            user.setGrade(registerData.get("grade"));
            user.setMajor(registerData.get("major"));
            user.setGender(registerData.get("gender"));
            user.setRole("user");
            user.setStatus(1);

            User registeredUser = userService.register(user);

            if (registeredUser != null) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getRole());

                // 构建响应数据
                Map<String, Object> response = new HashMap<>();
                Map<String, Object> userInfo = new HashMap<>();

                userInfo.put("id", registeredUser.getId());
                userInfo.put("username", registeredUser.getUsername());
                userInfo.put("realName", registeredUser.getRealName());
                userInfo.put("nickname", registeredUser.getNickname());
                userInfo.put("role", registeredUser.getRole());
                userInfo.put("email", registeredUser.getEmail());
                userInfo.put("phone", registeredUser.getPhone());
                userInfo.put("college", registeredUser.getCollege());
                userInfo.put("grade", registeredUser.getGrade());
                userInfo.put("major", registeredUser.getMajor());
                userInfo.put("gender", registeredUser.getGender());

                response.put("user", userInfo);
                response.put("token", token);

                return Map.of("code", 200, "msg", "注册成功", "data", response);
            } else {
                return Map.of("code", 400, "msg", "注册失败，用户名已存在", "data", null);
            }
        } catch (Exception e) {
            log.error("注册失败", e);
            return Map.of("code", 500, "msg", "注册失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 这里可以实现Token黑名单机制
            // 目前简单返回成功，前端删除本地Token即可
            return Map.of("code", 200, "msg", "登出成功", "data", null);
        } catch (Exception e) {
            log.error("登出失败", e);
            return Map.of("code", 500, "msg", "登出失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username/{username}")
    public Map<String, Object> checkUsername(@PathVariable String username) {
        try {
            boolean exists = userService.checkUsernameExists(username);
            return Map.of("code", 200, "msg", "success", "data", exists);
        } catch (Exception e) {
            log.error("检查用户名失败", e);
            return Map.of("code", 500, "msg", "检查失败：" + e.getMessage(), "data", false);
        }
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone/{phone}")
    public Map<String, Object> checkPhone(@PathVariable String phone) {
        try {
            boolean exists = userService.checkPhoneExists(phone);
            return Map.of("code", 200, "msg", "success", "data", exists);
        } catch (Exception e) {
            log.error("检查手机号失败", e);
            return Map.of("code", 500, "msg", "检查失败：" + e.getMessage(), "data", false);
        }
    }

    /**
     * 获取用户基本信息（不需要认证）
     */
    @GetMapping("/user/basic-info/{userId}")
    public Map<String, Object> getUserBasicInfo(@PathVariable Long userId) {
        try {
            log.info("获取用户基本信息 - 用户ID: {}", userId);

            // 获取用户基本信息
            User user = userService.getUserById(userId);
            if (user == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 404);
                result.put("msg", "用户不存在");
                result.put("data", null);
                return result;
            }

            // 获取真实的发布订单数
            int publishCount = userService.getPublishTaskCount(userId);
            // 获取真实的好评率
            int goodRate = userService.calculateGoodRate(userId);
            // 获取评价历史
            List<UserReview> reviews = userService.getGivenReviews(userId);

            // 构建响应数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("role", user.getRole());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("college", user.getCollege());
            userInfo.put("grade", user.getGrade());
            userInfo.put("registerTime", user.getRegisterTime());
            userInfo.put("signature", user.getSignature());
            userInfo.put("publishCount", publishCount);
            userInfo.put("goodRate", goodRate);

            // 处理评价历史
            List<Map<String, Object>> reviewList = new ArrayList<>();
            if (reviews != null && !reviews.isEmpty()) {
                for (UserReview review : reviews) {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("id", review.getId());
                    reviewMap.put("reviewerId", review.getReviewerId());
                    reviewMap.put("revieweeId", review.getRevieweeId());
                    reviewMap.put("taskId", review.getTaskId());
                    reviewMap.put("rating", review.getRating());
                    reviewMap.put("content", review.getContent());
                    reviewMap.put("tags", review.getTags());
                    reviewMap.put("createTime", review.getCreateTime());
                    
                    // 获取被评价人的信息
                    User reviewee = userService.getUserById(review.getRevieweeId());
                    if (reviewee != null) {
                        reviewMap.put("revieweeName", reviewee.getNickname() != null ? reviewee.getNickname() : reviewee.getRealName() != null ? reviewee.getRealName() : reviewee.getUsername());
                        reviewMap.put("revieweeAvatar", reviewee.getAvatar());
                    }
                    
                    reviewList.add(reviewMap);
                }
            }
            userInfo.put("reviews", reviewList);

            // 构建评分统计
            Map<String, Object> ratings = new HashMap<>();
            ratings.put("speed", 5.0);
            ratings.put("attitude", 5.0);
            ratings.put("quality", 5.0);
            userInfo.put("ratings", ratings);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", userInfo);
            return result;
        } catch (Exception e) {
            log.error("获取用户基本信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
            result.put("data", null);
            return result;
        }
    }

    /**
     * 获取用户详细信息（包含统计数据和评价）
     */
    @GetMapping("/user/profile")
    public Map<String, Object> getUserProfile(HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Long userId = (Long) request.getAttribute("currentUserId");
            log.info("获取用户详细信息 - 用户ID: {}", userId);

            // 检查userId是否为null
            if (userId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("msg", "用户未认证");
                result.put("data", null);
                return result;
            }

            // 获取用户基本信息
            User user = userService.getUserById(userId);
            if (user == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 404);
                result.put("msg", "用户不存在");
                result.put("data", null);
                return result;
            }

            // 获取用户收到的评价
            List<UserReview> receivedReviews = userService.getReceivedReviews(userId);
            // 获取用户发出的评价
            List<UserReview> givenReviews = userService.getGivenReviews(userId);

            // 计算好评率
            int totalReviews = receivedReviews != null ? receivedReviews.size() : 0;
            int goodReviews = 0;
            if (receivedReviews != null) {
                for (UserReview review : receivedReviews) {
                    if (review.getRating() >= 4) {
                        goodReviews++;
                    }
                }
            }
            int goodRate = totalReviews > 0 ? (goodReviews * 100) / totalReviews : 0;

            // 构建响应数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("role", user.getRole());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("college", user.getCollege());
            userInfo.put("grade", user.getGrade());
            userInfo.put("registerTime", user.getRegisterTime());
            userInfo.put("signature", user.getSignature());
            userInfo.put("goodRate", goodRate);
            userInfo.put("receivedReviews", receivedReviews != null ? receivedReviews : new ArrayList<>());
            userInfo.put("givenReviews", givenReviews != null ? givenReviews : new ArrayList<>());

            // 模拟发布订单数（实际项目中应该从数据库查询）
            userInfo.put("publishCount", 15);

            // 使用HashMap避免Map.of()的null值限制
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", userInfo);
            return result;
        } catch (Exception e) {
            log.error("获取用户详细信息失败", e);
            return Map.of("code", 500, "msg", "获取失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 更新用户资料
     */
    @PostMapping("/user/update-profile")
    public Map<String, Object> updateUserProfile(@RequestBody User user, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            Long userId = (Long) request.getAttribute("currentUserId");
            log.info("更新用户资料 - 用户ID: {}", userId);

            // 检查userId是否为null
            if (userId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("msg", "用户未认证");
                result.put("data", null);
                return result;
            }

            // 确保只能更新自己的资料
            user.setId(userId);

            // 更新用户资料
            User updatedUser = userService.updateUser(user);
            if (updatedUser != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "更新成功");
                result.put("data", updatedUser);
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 500);
                result.put("msg", "更新失败");
                result.put("data", null);
                return result;
            }
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            return Map.of("code", 500, "msg", "更新失败：" + e.getMessage(), "data", null);
        }
    }
}
