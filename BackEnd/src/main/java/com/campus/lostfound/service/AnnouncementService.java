package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Announcement;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {
    
    /**
     * 获取所有公告
     */
    List<Announcement> getAllAnnouncements();
    
    /**
     * 获取最新公告
     */
    Announcement getLatestAnnouncement();
    
    /**
     * 发布公告
     */
    Announcement publishAnnouncement(Announcement announcement);
    
    /**
     * 更新公告
     */
    boolean updateAnnouncement(Long id, Announcement announcement);
    
    /**
     * 删除公告
     */
    boolean deleteAnnouncement(Long id);
}
