package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 跑腿任务实体类
 */
@Data
@TableName("errand_tasks")
public class ErrandTask {
    private Long id;
    private String taskNo;
    private String type;
    private String title;
    private String description;
    private Long publisherId;
    private Long runnerId;
    private String pickupAddress;
    private String pickupDetail;
    private Double pickupLat;
    private Double pickupLng;
    private String deliveryAddress;
    private String deliveryDetail;
    private Double deliveryLat;
    private Double deliveryLng;
    private Date expectedTime;
    private Double price;
    private String contactPhone;
    private String remark;
    private String images;
    private String status;
    private Date publishTime;
    private Date acceptTime;
    private Date startTime;
    private Date completeTime;
    private Date cancelTime;
    private String cancelReason;
}
