package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户统计实体类
 */
@Data
@TableName("user_statistics")
public class UserStatistics {
    private Long id;
    private Long userId;
    private Integer totalLost;
    private Integer totalFound;
    private Integer recovered;
    private Integer publishTasks;
    private Integer acceptTasks;
    private Integer completeTasks;
    private Integer unreadNotifications;
    private Date updateTime;
}
