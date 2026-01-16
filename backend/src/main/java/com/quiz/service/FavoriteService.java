package com.quiz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.quiz.dto.FavoriteWithDetailDto;
import com.quiz.entity.Favorite;

import java.util.List;

/**
 * 收藏服务接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     */
    Favorite addFavorite(Favorite favorite);

    /**
     * 获取用户收藏列表
     */
    List<Favorite> getUserFavorites(Long userId, int current, int size, String keyword);

    /**
     * 获取用户收藏列表（分页，带题目详情）
     */
    IPage<FavoriteWithDetailDto> getUserFavoritesWithDetails(Long userId, int current, int size, String keyword);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, Long questionId);

    /**
     * 取消收藏
     */
    boolean removeFavorite(Long userId, Long questionId);

    /**
     * 检查收藏状态
     */
    boolean checkFavoriteStatus(Long userId, Long questionId);

    /**
     * 获取收藏的题目ID列表
     */
    List<Long> getFavoriteQuestionIds(Long userId);

    /**
     * 获取指定题库的收藏题目ID列表
     */
    List<Long> getFavoriteQuestionIdsByBank(Long userId, Long bankId);

    /**
     * 批量删除收藏
     */
    void batchRemoveFavorites(Long userId, List<Long> ids);

    /**
     * 获取用户收藏数量
     */
    long getUserFavoriteCount(Long userId);

    /**
     * 按题库分组获取收藏统计
     */
    List<java.util.Map<String, Object>> getStatsByBank(Long userId);
}
