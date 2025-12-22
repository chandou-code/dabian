package com.campus.lostfound.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 * 处理实体类中带有 @TableField(fill = FieldFill.INSERT) 或 @TableField(fill = FieldFill.INSERT_UPDATE) 注解的字段
 */
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        
        // 填充更新时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
