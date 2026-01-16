package com.quiz.service.impl;

import com.quiz.dto.QuestionImportResult;
import com.quiz.service.QuestionImportService;
import com.quiz.strategy.QuestionImportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目导入服务实现类
 * 使用策略模式支持多种文件格式的导入
 *
 * @author Quiz System
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionImportServiceImpl implements QuestionImportService {

    private final QuestionImportContext importContext;

    @Override
    public QuestionImportResult importQuestions(String filePath, String fileName, Long bankId, Long userId) {
        log.info("开始导入题目：文件={}, 题库ID={}, 用户ID={}", fileName, bankId, userId);

        try {
            // 验证文件格式
            if (!importContext.isFileSupported(fileName)) {
                QuestionImportResult result = new QuestionImportResult();
                result.setMessage("不支持的文件格式：" + fileName);
                result.getErrors().add("支持的格式：" + String.join(", ", getSupportedFormats()));
                return result;
            }

            // 使用策略模式导入
            QuestionImportResult result = importContext.importQuestions(fileName, filePath, bankId, userId);

            log.info("题目导入完成：成功={}, 失败={}, 跳过={}",
                    result.getSuccessCount(), result.getErrorCount(), result.getSkipCount());

            return result;

        } catch (Exception e) {
            log.error("题目导入异常", e);
            QuestionImportResult result = new QuestionImportResult();
            result.setMessage("导入异常：" + e.getMessage());
            result.getErrors().add(e.getMessage());
            return result;
        }
    }

    @Override
    public List<String> getSupportedFormats() {
        return importContext.getSupportedExtensions();
    }

    @Override
    public boolean isFormatSupported(String fileName) {
        return importContext.isFileSupported(fileName);
    }
}
