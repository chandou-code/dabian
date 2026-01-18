package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.RunnerApplication;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.RunnerApplicationMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.RunnerApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 跑腿员申请服务实现类
 */
@Slf4j
@Service
public class RunnerApplicationServiceImpl extends ServiceImpl<RunnerApplicationMapper, RunnerApplication> implements RunnerApplicationService {

    @Autowired
    private RunnerApplicationMapper applicationMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean submitApplication(RunnerApplication application) {
        try {
            // 设置默认状态和申请时间
            application.setStatus("pending");
            application.setApplyTime(new Date());

            // 保存申请
            boolean result = save(application);
            log.info("提交跑腿员申请成功 - 用户ID: {}", application.getUserId());
            return result;
        } catch (Exception e) {
            log.error("提交跑腿员申请失败", e);
            return false;
        }
    }

    @Override
    public List<RunnerApplication> getApplicationList(Map<String, Object> params) {
        try {
            LambdaQueryWrapper<RunnerApplication> queryWrapper = new LambdaQueryWrapper<>();

            // 状态筛选
            if (params.containsKey("status") && params.get("status") != null) {
                queryWrapper.eq(RunnerApplication::getStatus, params.get("status"));
            }

            // 时间排序
            queryWrapper.orderByDesc(RunnerApplication::getApplyTime);

            // 分页
            if (params.containsKey("page") && params.containsKey("pageSize")) {
                int page = (int) params.get("page");
                int pageSize = (int) params.get("pageSize");
                queryWrapper.last("LIMIT " + (page - 1) * pageSize + ", " + pageSize);
            }

            List<RunnerApplication> applications = list(queryWrapper);

            // 加载关联的用户信息
            for (RunnerApplication application : applications) {
                User user = userMapper.selectById(application.getUserId());
                application.setUser(user);
                if (application.getReviewerId() != null) {
                    User reviewer = userMapper.selectById(application.getReviewerId());
                    application.setReviewer(reviewer);
                }
            }

            return applications;
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return null;
        }
    }

    @Override
    public long getApplicationCount(Map<String, Object> params) {
        try {
            LambdaQueryWrapper<RunnerApplication> queryWrapper = new LambdaQueryWrapper<>();

            // 状态筛选
            if (params.containsKey("status") && params.get("status") != null) {
                queryWrapper.eq(RunnerApplication::getStatus, params.get("status"));
            }

            return count(queryWrapper);
        } catch (Exception e) {
            log.error("获取申请总数失败", e);
            return 0;
        }
    }

    @Override
    public RunnerApplication getApplicationById(Long id) {
        try {
            RunnerApplication application = getById(id);
            if (application != null) {
                // 加载用户信息
                User user = userMapper.selectById(application.getUserId());
                application.setUser(user);
                if (application.getReviewerId() != null) {
                    User reviewer = userMapper.selectById(application.getReviewerId());
                    application.setReviewer(reviewer);
                }
            }
            return application;
        } catch (Exception e) {
            log.error("获取申请详情失败", e);
            return null;
        }
    }

    @Override
    public RunnerApplication getApplicationByUserId(Long userId) {
        try {
            LambdaQueryWrapper<RunnerApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RunnerApplication::getUserId, userId);
            return getOne(queryWrapper);
        } catch (Exception e) {
            log.error("根据用户ID获取申请失败", e);
            return null;
        }
    }

    @Override
    public boolean approveApplication(Long id, Long reviewerId, String comment) {
        try {
            RunnerApplication application = getById(id);
            if (application != null) {
                application.setStatus("approved");
                application.setReviewerId(reviewerId);
                application.setReviewComment(comment);
                application.setReviewTime(new Date());

                // 更新申请状态
                boolean result = updateById(application);
                log.info("批准跑腿员申请成功 - 申请ID: {}, 审核人ID: {}", id, reviewerId);
                return result;
            }
            return false;
        } catch (Exception e) {
            log.error("批准跑腿员申请失败", e);
            return false;
        }
    }

    @Override
    public boolean rejectApplication(Long id, Long reviewerId, String comment) {
        try {
            RunnerApplication application = getById(id);
            if (application != null) {
                application.setStatus("rejected");
                application.setReviewerId(reviewerId);
                application.setReviewComment(comment);
                application.setReviewTime(new Date());

                // 更新申请状态
                boolean result = updateById(application);
                log.info("拒绝跑腿员申请成功 - 申请ID: {}, 审核人ID: {}", id, reviewerId);
                return result;
            }
            return false;
        } catch (Exception e) {
            log.error("拒绝跑腿员申请失败", e);
            return false;
        }
    }
}
