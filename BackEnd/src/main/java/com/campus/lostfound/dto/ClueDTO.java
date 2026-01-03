package com.campus.lostfound.dto;

import lombok.Data;
import java.util.List;

/**
 * 线索DTO
 */
@Data
public class ClueDTO {
    private Long id;
    private Long itemId;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String content;
    private List<String> images;
    private String status; // pending, reviewed, useful, invalid
    private String createdAt;
}