package com.quiz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quiz.dto.FavoriteWithDetailDto;
import com.quiz.dto.Result;
import com.quiz.entity.Favorite;
import com.quiz.service.FavoriteService;
import com.quiz.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏题目
     */
    @PostMapping
    public Result<Favorite> addFavorite(@RequestBody Favorite favorite) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            favorite.setUserId(userId);
            
            Favorite result = favoriteService.addFavorite(favorite);
            return Result.success("收藏成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeFavorite(@PathVariable Long id) {
        try {
            favoriteService.removeById(id);
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通过题目ID取消收藏
     */
    @DeleteMapping("/question/{questionId}")
    public Result<Void> removeFavoriteByQuestionId(@PathVariable Long questionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            boolean success = favoriteService.removeFavorite(userId, questionId);
            if (success) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取收藏列表
     */
    @GetMapping
    public Result<IPage<FavoriteWithDetailDto>> getFavorites(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            IPage<FavoriteWithDetailDto> favorites = favoriteService.getUserFavoritesWithDetails(userId, current, size, keyword);
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新收藏备注
     */
    @PutMapping("/{id}/notes")
    public Result<Favorite> updateNotes(@PathVariable Long id, @RequestBody String notes) {
        try {
            Favorite favorite = new Favorite();
            favorite.setId(id);
            favorite.setNotes(notes);
            favoriteService.updateById(favorite);
            return Result.success("更新成功", favorite);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查题目是否已收藏
     */
    @GetMapping("/check/{questionId}")
    public Result<Boolean> checkFavoriteStatus(@PathVariable Long questionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean isFavorited = favoriteService.checkFavoriteStatus(userId, questionId);
            return Result.success(isFavorited);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取收藏的题目ID列表
     */
    @GetMapping("/question-ids")
    public Result<List<Long>> getFavoriteQuestionIds() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<Long> questionIds = favoriteService.getFavoriteQuestionIds(userId);
            return Result.success(questionIds);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除收藏
     */
    @DeleteMapping("/batch")
    public Result<Void> batchRemoveFavorites(@RequestBody List<Long> ids) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            favoriteService.batchRemoveFavorites(userId, ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 按题库分组获取收藏统计
     */
    @GetMapping("/stats/by-bank")
    public Result<java.util.List<java.util.Map<String, Object>>> getStatsByBank() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            var stats = favoriteService.getStatsByBank(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
