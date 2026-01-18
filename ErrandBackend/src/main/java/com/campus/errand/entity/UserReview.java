package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户评价实体类
 */
@Data
@TableName("user_reviews")
public class UserReview {
    private Long id;
    private Long reviewerId;
    private Long revieweeId;
    private Long taskId;
    private Double rating;
    private String content;
    private String tags;
    private Date createTime;
}
