package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 匹配记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("matches")
public class Match implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 匹配记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 失物ID
     */
    @TableField("lost_item_id")
    private Long lostItemId;

    /**
     * 招领ID
     */
    @TableField("found_item_id")
    private Long foundItemId;

    /**
     * 匹配分数（0-100）
     */
    @TableField("match_score")
    private Double matchScore;

    /**
     * 相似度详情，JSON格式
     */
    @TableField("similarity_details")
    private String similarityDetails;

    /**
     * 匹配状态：pending-待确认，confirmed-已确认，rejected-已拒绝
     */
    @TableField("status")
    private String status;

    /**
     * 创建者ID（系统自动或手动创建）
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 确认时间
     */
    @TableField("confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 确认者ID
     */
    @TableField("confirmed_by")
    private Long confirmedBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}