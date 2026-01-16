package com.quiz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.dto.FavoriteWithDetailDto;
import com.quiz.entity.Favorite;
import com.quiz.entity.Question;
import com.quiz.entity.QuestionBank;
import com.quiz.mapper.FavoriteMapper;
import com.quiz.service.FavoriteService;
import com.quiz.service.QuestionBankService;
import com.quiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final QuestionService questionService;
    private final QuestionBankService questionBankService;

    @Override
    public Favorite addFavorite(Favorite favorite) {
        // 检查是否已收藏
        if (isFavorited(favorite.getUserId(), favorite.getQuestionId())) {
            throw new RuntimeException("题目已收藏");
        }
        
        favorite.setCreatedAt(LocalDateTime.now());
        save(favorite);
        return favorite;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId, int current, int size, String keyword) {
        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);

        // 按收藏备注搜索
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Favorite::getNotes, keyword);
        }

        queryWrapper.orderByDesc(Favorite::getCreatedAt);
        return page(page, queryWrapper).getRecords();
    }

    @Override
    public IPage<FavoriteWithDetailDto> getUserFavoritesWithDetails(Long userId, int current, int size, String keyword) {
        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);

        queryWrapper.orderByDesc(Favorite::getCreatedAt);

        IPage<Favorite> favoritePage = page(page, queryWrapper);

        // 转换为包含题目详情的DTO
        IPage<FavoriteWithDetailDto> resultPage = new Page<>(current, size, favoritePage.getTotal());

        List<FavoriteWithDetailDto> dtoList = favoritePage.getRecords().stream().map(favorite -> {
            FavoriteWithDetailDto dto = new FavoriteWithDetailDto();
            BeanUtils.copyProperties(favorite, dto);

            // 获取题目详情
            Question question = questionService.getById(favorite.getQuestionId());
            if (question != null) {
                FavoriteWithDetailDto.QuestionDetailDto questionDto = new FavoriteWithDetailDto.QuestionDetailDto();
                BeanUtils.copyProperties(question, questionDto);
                // 将枚举转换为字符串
                if (question.getType() != null) {
                    questionDto.setType(question.getType().name());
                }
                dto.setQuestion(questionDto);
            }

            return dto;
        }).toList();

        // 如果有关键词，过滤题目内容
        if (StrUtil.isNotBlank(keyword)) {
            dtoList = dtoList.stream()
                .filter(dto -> {
                    if (dto.getQuestion() == null) return false;

                    String content = dto.getQuestion().getContent() != null ? dto.getQuestion().getContent() : "";
                    String notes = dto.getNotes() != null ? dto.getNotes() : "";

                    return content.contains(keyword) || notes.contains(keyword);
                })
                .toList();

            // 重新计算总数
            resultPage.setTotal(dtoList.size());
        }

        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public boolean isFavorited(Long userId, Long questionId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                   .eq(Favorite::getQuestionId, questionId);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long questionId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                   .eq(Favorite::getQuestionId, questionId);
        return remove(queryWrapper);
    }

    @Override
    public boolean checkFavoriteStatus(Long userId, Long questionId) {
        return isFavorited(userId, questionId);
    }

    @Override
    public List<Long> getFavoriteQuestionIds(Long userId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                   .select(Favorite::getQuestionId);

        return list(queryWrapper).stream()
                .map(Favorite::getQuestionId)
                .toList();
    }

    @Override
    public List<Long> getFavoriteQuestionIdsByBank(Long userId, Long bankId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                   .eq(Favorite::getBankId, bankId)
                   .select(Favorite::getQuestionId);

        return list(queryWrapper).stream()
                .map(Favorite::getQuestionId)
                .toList();
    }

    @Override
    public void batchRemoveFavorites(Long userId, List<Long> ids) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                   .in(Favorite::getId, ids);
        remove(queryWrapper);
    }

    @Override
    public long getUserFavoriteCount(Long userId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        return count(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getStatsByBank(Long userId) {
        // 获取用户所有收藏
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        List<Favorite> favorites = list(queryWrapper);

        // 按bankId分组统计
        Map<Long, Long> bankCountMap = favorites.stream()
                .filter(f -> f.getBankId() != null)
                .collect(Collectors.groupingBy(Favorite::getBankId, Collectors.counting()));

        // 获取题库信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : bankCountMap.entrySet()) {
            Long bankId = entry.getKey();
            Long count = entry.getValue();

            QuestionBank bank = questionBankService.getById(bankId);
            if (bank != null) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("bankId", bankId);
                stats.put("bankName", bank.getName());
                stats.put("count", count);
                result.add(stats);
            }
        }

        // 按数量降序排序
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }
}
