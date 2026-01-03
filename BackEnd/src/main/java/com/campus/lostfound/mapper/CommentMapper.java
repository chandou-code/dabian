package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE item_comments SET like_count = like_count + 1 WHERE id = #{commentId}")
    void incrementLikeCount(Long commentId);
    
    /**
     * 减少点赞数
     */
    @Update("UPDATE item_comments SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId}")
    void decrementLikeCount(Long commentId);
}