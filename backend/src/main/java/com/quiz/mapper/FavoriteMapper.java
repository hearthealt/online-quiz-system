package com.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
