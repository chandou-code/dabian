package com.campus.lostfound.common.constants;

/**
 * 状态常量类
 */
public class StatusConstants {
    
    /**
     * 用户状态
     */
    public static final int USER_DISABLED = 0;
    public static final int USER_ENABLED = 1;
    
    /**
     * 用户角色
     */
    public static final String ROLE_USER = "user";
    public static final String ROLE_REVIEWER = "reviewer";
    public static final String ROLE_ADMIN = "admin";
    
    /**
     * 物品类型
     */
    public static final String ITEM_TYPE_LOST = "lost";
    public static final String ITEM_TYPE_FOUND = "found";
    
    /**
     * 物品状态
     */
    public static final String ITEM_STATUS_PENDING = "pending";
    public static final String ITEM_STATUS_APPROVED = "approved";
    public static final String ITEM_STATUS_REJECTED = "rejected";
    public static final String ITEM_STATUS_FOUND = "found";
    public static final String ITEM_STATUS_RECOVERED = "claimed";
    
    /**
     * 审核动作
     */
    public static final String REVIEW_ACTION_APPROVE = "approve";
    public static final String REVIEW_ACTION_REJECT = "reject";
    public static final String REVIEW_ACTION_APPROVED = "approved";
    public static final String REVIEW_ACTION_REJECTED = "rejected";
    
    /**
     * 通知类型
     */
    public static final String NOTIFICATION_TYPE_SYSTEM = "system";
    public static final String NOTIFICATION_TYPE_REVIEW = "review";
    public static final String NOTIFICATION_TYPE_FOUND = "found";
    
    /**
     * 通知状态
     */
    public static final int NOTIFICATION_UNREAD = 0;
    public static final int NOTIFICATION_READ = 1;
    
    /**
     * 删除标记
     */
    public static final int NOT_DELETED = 0;
    public static final int DELETED = 1;
    
    /**
     * 配置类型
     */
    public static final String CONFIG_TYPE_STRING = "string";
    public static final String CONFIG_TYPE_NUMBER = "number";
    public static final String CONFIG_TYPE_BOOLEAN = "boolean";
    public static final String CONFIG_TYPE_JSON = "json";
    
    /**
     * 系统配置标记
     */
    public static final int NOT_SYSTEM = 0;
    public static final int IS_SYSTEM = 1;
    
    /**
     * 性别
     */
    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    
    private StatusConstants() {}
}