package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 系统公告实体类
 */
@Data
@TableName("notices")
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Integer priority;
    private Integer status;
    private Long publisherId;
    private Integer viewCount;
    private Date createTime;
}
