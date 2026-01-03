package com.campus.lostfound.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 物品详情DTO
 */
@Data
public class ItemDetailDTO {
    private Long id;
    private String itemName;
    private String description;
    private String category;
    private String type; // lost 或 found
    private LocalDate eventTime;
    private String location;
    private String contact;
    private List<String> images;
    private String status;
    private Long submitterId;
    private String userName;
    private String userPhone; // 脱敏后的手机号
    private String userEmail; // 脱敏后的邮箱
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    // 统计信息
    private int viewCount;
    private int commentCount;
    private int clueCount;
}