package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 跑腿员申请表实体类
 */
@Data
@TableName("runner_applications")
public class RunnerApplication {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 服务范围
     */
    @TableField("service_area")
    private String serviceArea;

    /**
     * 工作时间
     */
    @TableField("work_time")
    private String workTime;

    /**
     * 服务标签（JSON格式）
     */
    @TableField("service_tags")
    private String serviceTags;

    /**
     * 个人简介
     */
    @TableField("introduction")
    private String introduction;

    /**
     * 学生证照片URL
     */
    @TableField("student_id_photo")
    private String studentIdPhoto;

    /**
     * 申请状态：pending/approved/rejected
     */
    @TableField("status")
    private String status;

    /**
     * 审核人ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 申请时间
     */
    @TableField(value = "apply_time", fill = FieldFill.INSERT)
    private Date applyTime;

    /**
     * 审核时间
     */
    @TableField("review_time")
    private Date reviewTime;

    /**
     * 用户信息（关联查询）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 审核人信息（关联查询）
     */
    @TableField(exist = false)
    private User reviewer;
}
