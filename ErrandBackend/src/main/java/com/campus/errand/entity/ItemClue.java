package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 物品线索实体类
 */
@Data
@TableName("item_clues")
public class ItemClue {
    private Long id;
    private Long itemId;
    private Long providerId;
    private String content;
    private String contact;
    private String status;
    private Long reviewerId;
    private Date createTime;
}
