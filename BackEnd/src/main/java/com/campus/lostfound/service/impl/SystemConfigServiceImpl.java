package com.campus.lostfound.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.common.constants.StatusConstants;
import com.campus.lostfound.entity.SystemConfig;
import com.campus.lostfound.mapper.SystemConfigMapper;
import com.campus.lostfound.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    @Override
    public String getConfigValue(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return null;
        }
        
        SystemConfig config = baseMapper.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return StrUtil.isNotBlank(value) ? value : defaultValue;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(String configKey, String configValue) {
        if (StrUtil.isBlank(configKey) || configValue == null) {
            return false;
        }
        
        SystemConfig config = baseMapper.selectByKey(configKey);
        if (config != null) {
            return baseMapper.updateConfigValue(configKey, configValue) > 0;
        } else {
            // 创建新配置
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);

            return save(config);
        }
    }
    
    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = list(new LambdaQueryWrapper<SystemConfig>()
            .eq(SystemConfig::getDeleted, StatusConstants.NOT_DELETED)
            .orderByAsc(SystemConfig::getConfigKey));
        
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return configMap;
    }
    
    @Override
    public List<SystemConfig> getConfigsByType(String type) {
        if (StrUtil.isBlank(type)) {
            return new ArrayList<>();
        }
        
        return list(new LambdaQueryWrapper<SystemConfig>()
            .eq(SystemConfig::getConfigType, type)
            .eq(SystemConfig::getDeleted, StatusConstants.NOT_DELETED)
            .orderByAsc(SystemConfig::getConfigKey));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            boolean success = updateConfig(entry.getKey(), entry.getValue());
            if (!success) {
                allSuccess = false;
                log.warn("更新配置失败：{} = {}", entry.getKey(), entry.getValue());
            }
        }
        
        return allSuccess;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSystemConfigs() {
        // 默认系统配置
        Map<String, Object> defaultConfigs = new HashMap<>();
        
        // 基本配置
        defaultConfigs.put("site_name", "校园失物招领系统");
        defaultConfigs.put("site_description", "基于SpringBoot + Vue的校园失物招领平台");
        defaultConfigs.put("contact_email", "campus@example.com");
        
        // 分类配置
        defaultConfigs.put("categories", "[\"证件类\",\"电子设备\",\"生活用品\",\"学习用品\",\"服装配饰\",\"其他\"]");
        defaultConfigs.put("locations", "[\"教学楼\",\"图书馆\",\"食堂\",\"宿舍\",\"体育馆\",\"实验室\",\"校园道路\",\"其他\"]");
        
        // 文件上传配置
        defaultConfigs.put("max_image_count", "6");
        defaultConfigs.put("upload_image_size", "10485760");
        defaultConfigs.put("allowed_image_types", "[\"jpg\",\"jpeg\",\"png\",\"gif\"]");
        
        // 系统设置
        defaultConfigs.put("review_required", "true");
        defaultConfigs.put("auto_approve_users", "false");
        defaultConfigs.put("max_items_per_day", "10");
        
        // 通知设置
        defaultConfigs.put("email_notification_enabled", "false");
        defaultConfigs.put("sms_notification_enabled", "false");
        
        // 保存配置
        for (Map.Entry<String, Object> entry : defaultConfigs.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue().toString();
            
            SystemConfig existingConfig = baseMapper.selectByKey(configKey);
            if (existingConfig == null) {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);

                config.setConfigType(getConfigTypeByValue(configValue));
                config.setDescription(getConfigDescription(configKey));
                save(config);
            }
        }
    }
    
    /**
     * 根据值判断配置类型
     */
    private String getConfigTypeByValue(String value) {
        if (value == null) {
            return StatusConstants.CONFIG_TYPE_STRING;
        }
        
        if (value.equals("true") || value.equals("false")) {
            return StatusConstants.CONFIG_TYPE_BOOLEAN;
        }
        
        if (value.matches("^\\d+$")) {
            return StatusConstants.CONFIG_TYPE_NUMBER;
        }
        
        if (value.startsWith("[") || value.startsWith("{")) {
            return StatusConstants.CONFIG_TYPE_JSON;
        }
        
        return StatusConstants.CONFIG_TYPE_STRING;
    }
    
    /**
     * 获取配置描述
     */
    private String getConfigDescription(String configKey) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("site_name", "网站名称");
        descriptions.put("site_description", "网站描述");
        descriptions.put("contact_email", "联系邮箱");
        descriptions.put("categories", "物品分类配置");
        descriptions.put("locations", "地点分类配置");
        descriptions.put("max_image_count", "最大图片数量");
        descriptions.put("upload_image_size", "上传图片大小限制（字节）");
        descriptions.put("allowed_image_types", "允许的图片类型");
        descriptions.put("review_required", "是否需要审核");
        descriptions.put("auto_approve_users", "是否自动批准可信用户");
        descriptions.put("max_items_per_day", "每日最大发布数量");
        descriptions.put("email_notification_enabled", "是否启用邮件通知");
        descriptions.put("sms_notification_enabled", "是否启用短信通知");
        
        return descriptions.getOrDefault(configKey, "");
    }
    
    // ========== 实现新增的系统配置管理方法 ==========
    
    @Override
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 基本配置
        config.put("systemName", getConfigValue("system_name", "校园失物招领系统"));
        config.put("description", getConfigValue("description", "让失物回家，让爱心传递"));
        config.put("version", getConfigValue("version", "1.0.0"));
        config.put("maintenanceMode", Boolean.parseBoolean(getConfigValue("maintenance_mode", "false")));
        
        return config;
    }
    
    @Override
    public Map<String, Object> updateSystemConfig(Map<String, Object> configData) {
        Map<String, Object> updatedConfig = new HashMap<>();
        
        try {
            for (Map.Entry<String, Object> entry : configData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value != null) {
                    updateConfig(key, value.toString());
                    updatedConfig.put(key, value);
                }
            }
            
            return updatedConfig;
        } catch (Exception e) {
            log.error("更新系统配置失败", e);
            throw new RuntimeException("更新系统配置失败：" + e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> getEmailTemplates() {
        Map<String, String> templates = new HashMap<>();
        
        templates.put("approval", getConfigValue("email_template_approval", "您的失物招领信息已审核通过"));
        templates.put("rejection", getConfigValue("email_template_rejection", "您的失物招领信息未通过审核，原因：%reason%"));
        templates.put("recovery", getConfigValue("email_template_recovery", "您发布的失物招领有新的反馈，请查看"));
        
        return templates;
    }
    
    @Override
    public Map<String, String> updateEmailTemplates(Map<String, String> templates) {
        Map<String, String> updatedTemplates = new HashMap<>();
        
        try {
            for (Map.Entry<String, String> entry : templates.entrySet()) {
                String key = "email_template_" + entry.getKey();
                String value = entry.getValue();
                
                updateConfig(key, value);
                updatedTemplates.put(entry.getKey(), value);
            }
            
            return updatedTemplates;
        } catch (Exception e) {
            log.error("更新邮件模板失败", e);
            throw new RuntimeException("更新邮件模板失败：" + e.getMessage());
        }
    }
    
    @Override
    public java.util.List<Map<String, Object>> getAnnouncements() {
        java.util.List<Map<String, Object>> announcements = new ArrayList<>();
        
        // 这里简化处理，实际应该从数据库查询
        Map<String, Object> announcement1 = new HashMap<>();
        announcement1.put("id", 1L);
        announcement1.put("title", "系统升级通知");
        announcement1.put("content", "系统将于今晚进行升级维护");
        announcement1.put("status", "active");
        announcement1.put("created_at", "2024-01-01 10:00:00");
        announcements.add(announcement1);
        
        return announcements;
    }
    
    @Override
    public Map<String, Object> createAnnouncement(String title, String content, String status) {
        Map<String, Object> announcement = new HashMap<>();
        
        // 这里简化处理，实际应该插入数据库
        announcement.put("id", System.currentTimeMillis());
        announcement.put("title", title);
        announcement.put("content", content);
        announcement.put("status", status);
        announcement.put("created_at", java.time.LocalDateTime.now().toString());
        
        return announcement;
    }
    
    @Override
    public Map<String, Object> updateAnnouncement(Long id, String title, String content, String status) {
        Map<String, Object> announcement = new HashMap<>();
        
        // 这里简化处理，实际应该更新数据库
        announcement.put("id", id);
        announcement.put("title", title);
        announcement.put("content", content);
        announcement.put("status", status);
        announcement.put("updated_at", java.time.LocalDateTime.now().toString());
        
        return announcement;
    }
    
    @Override
    public void deleteAnnouncement(Long id) {
        // 这里简化处理，实际应该从数据库删除
        log.info("删除公告，ID: {}", id);
    }
    
    @Override
    public String exportStatistics(String type, String format, String startDate, String endDate) {
        // 这里简化处理，实际应该生成真实的导出文件
        String fileName = "statistics_" + type + "_" + System.currentTimeMillis() + "." + format;
        String filePath = "/tmp/" + fileName;
        
        log.info("导出统计数据，类型: {}, 格式: {}, 时间范围: {} - {}", type, format, startDate, endDate);
        
        return filePath;
    }
}