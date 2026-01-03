package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.Announcement;
import com.campus.lostfound.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/")
@CrossOrigin
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    
    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }
    
    /**
     * 获取所有公告（管理员权限）
     */
    @GetMapping("admin/announcements")
    public Result<List<Announcement>> getAllAnnouncements(
            @RequestHeader("Authorization") String token) {
        try {
            log.info("管理员获取所有公告");
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            log.error("获取所有公告失败", e);
            return Result.error("获取所有公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 发布公告（管理员权限）
     */
    @PostMapping("admin/announcements")
    public Result<Announcement> publishAnnouncement(
            @RequestBody Announcement announcement,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("管理员发布公告：{}", announcement.getTitle());
            Announcement publishedAnnouncement = announcementService.publishAnnouncement(announcement);
            return Result.success("发布成功", publishedAnnouncement);
        } catch (Exception e) {
            log.error("发布公告失败", e);
            return Result.error("发布公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新公告（管理员权限）
     */
    @PutMapping("admin/announcements/{id}")
    public Result<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement announcement,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("管理员更新公告：{}", id);
            boolean success = announcementService.updateAnnouncement(id, announcement);
            if (success) {
                Announcement updatedAnnouncement = announcementService.getById(id);
                return Result.success("更新成功", updatedAnnouncement);
            } else {
                return Result.error("更新失败：公告不存在");
            }
        } catch (Exception e) {
            log.error("更新公告失败", e);
            return Result.error("更新公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除公告（管理员权限）
     */
    @DeleteMapping("admin/announcements/{id}")
    public Result deleteAnnouncement(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("管理员删除公告：{}", id);
            boolean success = announcementService.deleteAnnouncement(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败：公告不存在");
            }
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return Result.error("删除公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取最新公告（所有用户）
     */
    @GetMapping("announcements/latest")
    public Result<Announcement> getLatestAnnouncement() {
        try {
            log.info("获取最新公告");
            Announcement announcement = announcementService.getLatestAnnouncement();
            return Result.success(announcement);
        } catch (Exception e) {
            log.error("获取最新公告失败", e);
            return Result.error("获取最新公告失败：" + e.getMessage());
        }
    }
}
