package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 失物招领物品实体类
 */
@Data
@TableName("items")
public class Item {
    private Long id;
    private String itemNo;
    private String type;
    private String itemName;
    private String category;
    private String description;
    private Date lostTime;
    private String location;
    private String contact;
    private Long publisherId;
    private String images;
    private String aiDescription;
    private String status;
    private Long reviewerId;
    private Date reviewTime;
    private String reviewComment;
    private Date publishTime;
}
