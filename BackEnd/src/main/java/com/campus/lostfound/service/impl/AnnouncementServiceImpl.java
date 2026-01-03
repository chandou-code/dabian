package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.entity.Announcement;
import com.campus.lostfound.mapper.AnnouncementMapper;
import com.campus.lostfound.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
    
    private final AnnouncementMapper announcementMapper;
    
    public AnnouncementServiceImpl(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }
    
    @Override
    public List<Announcement> getAllAnnouncements() {
        log.info("获取所有公告");
        return announcementMapper.selectList(null);
    }
    
    @Override
    public Announcement getLatestAnnouncement() {
        log.info("获取最新公告");
        return announcementMapper.getLatestAnnouncement();
    }
    
    @Override
    public Announcement publishAnnouncement(Announcement announcement) {
        log.info("发布公告: {}", announcement.getTitle());
        
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        announcementMapper.insert(announcement);
        return announcement;
    }
    
    @Override
    public boolean updateAnnouncement(Long id, Announcement announcement) {
        log.info("更新公告: {}", id);
        
        announcement.setId(id);
        announcement.setUpdatedAt(LocalDateTime.now());
        
        int result = announcementMapper.updateById(announcement);
        return result > 0;
    }
    
    @Override
    public boolean deleteAnnouncement(Long id) {
        log.info("删除公告: {}", id);
        
        int result = announcementMapper.deleteById(id);
        return result > 0;
    }
}
