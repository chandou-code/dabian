package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 失物招领物品实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("items")
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;



    /**
     * 物品名称
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 物品类别
     */
    @TableField("category")
    private String category;

    /**
     * 物品描述
     */
    @TableField("description")
    private String description;

    /**
     * 物品图片URL数组，JSON格式
     */
    @TableField("images")
    private String images;

    /**
     * 联系方式
     */
    @TableField("contact")
    private String contact;

    /**
     * 地点（丢失地点或发现地点）
     */
    @TableField("location")
    private String location;

    /**
     * 事件时间（丢失时间或发现时间）
     */
    @TableField("event_time")
    private LocalDate eventTime;



    /**
     * 类型：lost-失物，found-招领
     */
    @TableField("type")
    private String type;

    /**
     * 状态：pending-待审核，approved-已通过，rejected-已驳回，claimed-已认领
     */
    @TableField("status")
    private String status;

    /**
     * 发布者ID
     */
    @TableField("submitter_id")
    private Long submitterId;

    /**
     * 发布者姓名（非数据库字段，仅用于展示）
     */
    @TableField(exist = false)
    private String submitterName;

    /**
     * 审核者ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;



    /**
     * 审核原因
     */
    @TableField("review_reason")
    private String reviewReason;



    /**
     * 匹配的物品ID
     */
    @TableField("matched_item_id")
    private Long matchedItemId;

    /**
     * 认领/找回时间
     */
    @TableField("recovered_at")
    private LocalDateTime recoveredAt;



    /**
     * 匹配时间
     */
    @TableField("matched_time")
    private LocalDateTime matchedTime;

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

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}