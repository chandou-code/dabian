package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("reviews")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物品ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 审核者ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核动作：approved-通过，rejected-驳回
     */
    @TableField("action")
    private String action;

    /**
     * 审核原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 审核时间
     */
    @TableField(value = "review_time", fill = FieldFill.INSERT)
    private LocalDateTime reviewTime;
}