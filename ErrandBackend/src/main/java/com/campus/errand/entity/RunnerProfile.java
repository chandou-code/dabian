package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 跑腿员档案实体类
 */
@Data
@TableName("runner_profiles")
public class RunnerProfile {
    private Long id;
    private Long userId;
    private String serviceTags;
    private String serviceArea;
    private String workTime;
    private Integer completeCount;
    private Double goodRate;
    private Double ratingSpeed;
    private Double ratingAttitude;
    private Double ratingQuality;
}
