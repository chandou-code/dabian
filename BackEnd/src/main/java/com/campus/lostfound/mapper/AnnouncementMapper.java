package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.Announcement;

/**
 * 公告Mapper
 */
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    
    /**
     * 获取最新公告
     */
    Announcement getLatestAnnouncement();
}
