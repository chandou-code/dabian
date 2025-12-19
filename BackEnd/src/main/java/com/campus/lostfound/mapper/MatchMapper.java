package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.Match;
import org.apache.ibatis.annotations.Mapper;

/**
 * 匹配记录Mapper接口
 */
@Mapper
public interface MatchMapper extends BaseMapper<Match> {
}