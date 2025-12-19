package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {
    
    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值（带默认值）
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 更新配置值
     */
    boolean updateConfig(String configKey, String configValue);
    
    /**
     * 获取所有配置
     */
    Map<String, String> getAllConfigs();
    
    /**
     * 获取分类配置
     */
    List<SystemConfig> getConfigsByType(String type);
    
    /**
     * 批量更新配置
     */
    boolean batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 初始化系统配置
     */
    void initSystemConfigs();
    
    // ========== 新增系统配置管理方法 ==========
    
    /**
     * 获取系统配置
     */
    Map<String, Object> getSystemConfig();
    
    /**
     * 更新系统配置
     */
    Map<String, Object> updateSystemConfig(Map<String, Object> configData);
    
    /**
     * 获取邮件模板
     */
    Map<String, String> getEmailTemplates();
    
    /**
     * 更新邮件模板
     */
    Map<String, String> updateEmailTemplates(Map<String, String> templates);
    
    /**
     * 获取公告列表
     */
    java.util.List<Map<String, Object>> getAnnouncements();
    
    /**
     * 创建公告
     */
    Map<String, Object> createAnnouncement(String title, String content, String status);
    
    /**
     * 更新公告
     */
    Map<String, Object> updateAnnouncement(Long id, String title, String content, String status);
    
    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);
    
    /**
     * 导出统计数据
     */
    String exportStatistics(String type, String format, String startDate, String endDate);
}