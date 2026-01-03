package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.Clue;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartFile;

/**
 * 线索Mapper接口
 */
@Mapper
public interface ClueMapper extends BaseMapper<Clue> {
}