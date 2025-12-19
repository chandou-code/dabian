package com.campus.lostfound.common;

/**
 * 响应状态码枚举
 */
public enum ResultCode {
    
    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    
    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已禁用"),
    TOKEN_EXPIRED(1005, "令牌已过期"),
    TOKEN_INVALID(1006, "令牌无效"),
    
    // 业务相关
    ITEM_NOT_FOUND(2001, "物品不存在"),
    ITEM_ALREADY_APPROVED(2002, "物品已审核"),
    ITEM_ALREADY_REJECTED(2003, "物品已驳回"),
    UPLOAD_FAILED(2004, "文件上传失败"),
    INVALID_FILE_TYPE(2005, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(2006, "文件大小超出限制"),
    
    // 审核相关
    NO_PERMISSION_TO_REVIEW(3001, "无审核权限"),
    REVIEW_COMPLETED(3002, "审核已完成"),
    
    // 系统相关
    SYSTEM_ERROR(9001, "系统错误"),
    DATABASE_ERROR(9002, "数据库错误"),
    NETWORK_ERROR(9003, "网络错误"),
    
    // 业务成功消息
    LOGIN_SUCCESS(1000, "登录成功"),
    LOGOUT_SUCCESS(1007, "退出成功"),
    REGISTER_SUCCESS(1008, "注册成功"),
    PUBLISH_SUCCESS(2000, "发布成功"),
    APPROVE_SUCCESS(3000, "审核通过"),
    REJECT_SUCCESS(3003, "审核驳回"),
    UPDATE_SUCCESS(4000, "更新成功"),
    DELETE_SUCCESS(4001, "删除成功");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}