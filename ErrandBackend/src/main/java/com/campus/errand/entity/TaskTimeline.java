package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 任务进度实体类
 */
@Data
@TableName("task_timelines")
public class TaskTimeline {
    private Long id;
    private Long taskId;
    private Integer step;
    private String title;
    private String description;
    private Date createTime;
}
