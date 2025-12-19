package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传记录Mapper接口
 */
@Mapper
public interface FileUploadMapper extends BaseMapper<FileUpload> {
}