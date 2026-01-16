package com.quiz.strategy;

import com.quiz.dto.QuestionImportResult;
import com.quiz.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目导入策略上下文
 * 使用策略模式管理不同格式的题目导入
 *
 * @author Quiz System
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionImportContext {

    private final List<QuestionImportStrategy> importStrategies;

    /**
     * 根据文件名获取合适的导入策略
     */
    private QuestionImportStrategy getStrategy(String fileName) {
        return importStrategies.stream()
                .filter(strategy -> strategy.supports(fileName))
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的文件格式：" + getFileExtension(fileName)));
    }

    /**
     * 导入题目到指定题库
     *
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param bankId   题库ID
     * @param userId   用户ID
     * @return 导入结果
     */
    public QuestionImportResult importQuestions(String fileName, String filePath, Long bankId, Long userId) {
        try {
            log.info("开始导入题目文件：{}，题库ID：{}，用户ID：{}", fileName, bankId, userId);

            QuestionImportStrategy strategy = getStrategy(fileName);
            log.info("使用导入策略：{}", strategy.getClass().getSimpleName());

            QuestionImportResult result = strategy.importQuestions(filePath, bankId, userId);

            log.info("题目导入完成，总数：{}，成功：{}，跳过：{}，错误：{}",
                    result.getTotalCount(), result.getSuccessCount(),
                    result.getSkipCount(), result.getErrorCount());

            return result;

        } catch (Exception e) {
            log.error("题目导入失败", e);
            QuestionImportResult result = new QuestionImportResult();
            result.setMessage("导入失败：" + e.getMessage());
            result.getErrors().add(e.getMessage());
            return result;
        }
    }

    /**
     * 获取所有支持的文件扩展名
     */
    public List<String> getSupportedExtensions() {
        return importStrategies.stream()
                .map(QuestionImportStrategy::getSupportedExtension)
                .collect(Collectors.toList());
    }

    /**
     * 获取策略映射（用于调试和监控）
     */
    public Map<String, String> getStrategyMappings() {
        return importStrategies.stream()
                .collect(Collectors.toMap(
                        QuestionImportStrategy::getSupportedExtension,
                        strategy -> strategy.getClass().getSimpleName()
                ));
    }

    /**
     * 验证文件格式是否支持
     */
    public boolean isFileSupported(String fileName) {
        return importStrategies.stream()
                .anyMatch(strategy -> strategy.supports(fileName));
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
