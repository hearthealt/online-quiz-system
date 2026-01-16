package com.quiz.strategy.impl;

import com.quiz.dto.QuestionImportResult;
import com.quiz.entity.Question;
import com.quiz.service.QuestionService;
import com.quiz.strategy.QuestionImportStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Excel格式题目导入策略
 *
 * Excel格式要求（第一行为表头）：
 * | 试题类型 | 题目 | 答案 | 解析 | 选项1 | 选项2 | 选项3 | ... |
 *
 * 说明：
 * - 试题类型：单选题/多选题/判断题/简答题 或 single/multiple/judge/essay
 * - 题目：必填
 * - 答案：必填（单选填选项序号如1/2/3或A/B/C，多选用逗号分隔如1,2,3或A,B,C，判断填"正确"/"错误"）
 * - 解析：可选
 * - 选项：从第5列开始，可以有任意多个选项
 *
 * @author Quiz System
 * @since 2024
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelQuestionImportStrategy implements QuestionImportStrategy {

    private final QuestionService questionService;

    @Value("${file.upload.path:/tmp/quiz-uploads}")
    private String uploadPath;

    @Override
    public QuestionImportResult importQuestions(String filePath, Long bankId, Long userId) {
        QuestionImportResult result = new QuestionImportResult();
        List<String> errors = new ArrayList<>();

        try {
            // 将相对路径转换为绝对路径
            String fullPath = convertToAbsolutePath(filePath);
            log.info("读取Excel文件: 相对路径={}, 绝对路径={}", filePath, fullPath);

            File file = new File(fullPath);
            if (!file.exists()) {
                result.setMessage("文件不存在: " + fullPath);
                errors.add("文件不存在: " + fullPath);
                result.setErrors(errors);
                return result;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                int lastRowNum = sheet.getLastRowNum();
                int successCount = 0;
                int skipCount = 0;
                int totalDataRows = 0;
                int sortOrder = questionService.countByBankId(bankId); // 从当前最大序号开始

                // 跳过表头行（第0行），从第1行开始处理数据
                for (int i = 1; i <= lastRowNum; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        skipCount++;
                        continue;
                    }

                    // 检查是否为空行
                    if (isEmptyRow(row)) {
                        continue;
                    }

                    totalDataRows++;

                    try {
                        Question question = parseQuestionFromRow(row, bankId, userId, sortOrder);
                        if (question != null) {
                            boolean saved = questionService.save(question);
                            if (saved) {
                                successCount++;
                                sortOrder++;
                            } else {
                                errors.add(String.format("第%d行保存失败", i + 1));
                            }
                        } else {
                            skipCount++;
                            errors.add(String.format("第%d行格式错误，已跳过", i + 1));
                        }
                    } catch (Exception e) {
                        skipCount++;
                        errors.add(String.format("第%d行处理异常：%s", i + 1, e.getMessage()));
                        log.error("处理第{}行时发生异常", i + 1, e);
                    }
                }

                result.setTotalCount(totalDataRows);
                result.setSuccessCount(successCount);
                result.setSkipCount(skipCount);
                result.setErrorCount(totalDataRows - successCount - skipCount);
                result.setErrors(errors);

                if (successCount > 0) {
                    result.setMessage(String.format("成功导入%d题，跳过%d题", successCount, skipCount));
                    // 更新题库题目数量
                    questionService.updateBankQuestionCount(bankId);
                } else {
                    result.setMessage("没有成功导入任何题目");
                }

                return result;
            }
        } catch (Exception e) {
            log.error("Excel文件解析失败", e);
            errors.add("Excel文件解析失败：" + e.getMessage());
            result.setErrors(errors);
            result.setMessage("文件解析失败");
            return result;
        }
    }

    /**
     * 检查是否为空行
     */
    private boolean isEmptyRow(Row row) {
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum <= 0) {
            return true;
        }
        for (int j = 0; j < lastCellNum; j++) {
            Cell cell = row.getCell(j);
            if (cell != null) {
                String cellValue = getCellStringValue(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将相对路径转换为绝对路径
     */
    private String convertToAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 若已是以配置的上传根路径开头，直接返回
        if (relativePath.startsWith(uploadPath)) {
            return relativePath;
        }

        // 针对以"/questions/"这种URL风格的"相对路径"做兼容
        if (relativePath.startsWith("/questions/") || relativePath.startsWith("questions/")) {
            String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            return Paths.get(uploadPath).resolve(normalized.replace("/", File.separator)).normalize().toString();
        }

        // 如果是系统层面的绝对路径，直接返回
        File file = new File(relativePath);
        if (file.isAbsolute()) {
            return relativePath;
        }

        // 移除相对路径开头的斜杠
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        String normalizedPath = path.replace("/", File.separator);
        return Paths.get(uploadPath).resolve(normalizedPath).normalize().toString();
    }

    @Override
    public String getSupportedExtension() {
        return "xlsx";
    }

    @Override
    public boolean supports(String filePath) {
        return filePath != null && (filePath.toLowerCase().endsWith(".xlsx") || filePath.toLowerCase().endsWith(".xls"));
    }

    /**
     * 从Excel行解析题目对象
     * Excel格式：试题类型 | 题目 | 答案 | 解析 | 选项1 | 选项2 | 选项3 | ...
     */
    private Question parseQuestionFromRow(Row row, Long bankId, Long userId, int sortOrder) {
        try {
            Question question = new Question();
            question.setBankId(bankId);
            question.setCreatedBy(userId);
            question.setSortOrder(sortOrder);
            question.setStatus(1);

            // 试题类型 (列0)
            String typeStr = getCellStringValue(row.getCell(0));
            if (typeStr == null || typeStr.trim().isEmpty()) {
                return null;
            }

            Question.QuestionType type = parseQuestionType(typeStr.trim());
            if (type == null) {
                log.warn("未知的题目类型：{}，支持的类型：单选题/多选题/判断题/简答题 或 single/multiple/judge/essay", typeStr);
                return null;
            }
            question.setType(type);

            // 题目内容 (列1)
            String content = getCellStringValue(row.getCell(1));
            if (content == null || content.trim().isEmpty()) {
                return null; // 题目内容是必需的
            }
            question.setContent(content);

            // 答案 (列2)
            String answer = getCellStringValue(row.getCell(2));
            if (answer == null || answer.trim().isEmpty()) {
                return null; // 正确答案是必需的
            }
            question.setCorrectAnswer(answer.trim());

            // 解析 (列3)
            String analysis = getCellStringValue(row.getCell(3));
            if (analysis != null && !analysis.trim().isEmpty()) {
                question.setAnalysis(analysis.trim());
            }

            // 选项 (列4开始，可以有任意多个)
            List<String> options = new ArrayList<>();
            int lastCellNum = row.getLastCellNum();
            for (int i = 4; i < lastCellNum; i++) {
                String option = getCellStringValue(row.getCell(i));
                if (option != null && !option.trim().isEmpty()) {
                    options.add(option.trim());
                }
            }

            // 根据题目类型设置选项
            if (type == Question.QuestionType.single || type == Question.QuestionType.multiple) {
                if (options.size() < 2) {
                    log.warn("选择题至少需要2个选项，当前只有{}个", options.size());
                    return null;
                }
                question.setOptions(options);
            } else if (type == Question.QuestionType.judge) {
                // 判断题默认选项
                if (options.isEmpty()) {
                    question.setOptions(Arrays.asList("正确", "错误"));
                } else {
                    question.setOptions(options);
                }
            }
            // 简答题不需要选项

            return question;

        } catch (Exception e) {
            log.error("解析Excel行失败", e);
            return null;
        }
    }

    /**
     * 解析题目类型，支持中文和英文两种格式
     */
    private Question.QuestionType parseQuestionType(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return null;
        }

        String normalized = typeStr.trim();

        // 支持中文格式
        if (normalized.contains("单选") || normalized.equals("单选题")) {
            return Question.QuestionType.single;
        } else if (normalized.contains("多选") || normalized.equals("多选题")) {
            return Question.QuestionType.multiple;
        } else if (normalized.contains("判断") || normalized.equals("判断题")) {
            return Question.QuestionType.judge;
        } else if (normalized.contains("简答") || normalized.equals("简答题")) {
            return Question.QuestionType.essay;
        }

        // 支持英文格式
        try {
            return Question.QuestionType.valueOf(normalized.toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 安全获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    switch (cell.getCachedFormulaResultType()) {
                        case STRING:
                            return cell.getStringCellValue().trim();
                        case NUMERIC:
                            double formulaValue = cell.getNumericCellValue();
                            if (formulaValue == (long) formulaValue) {
                                return String.valueOf((long) formulaValue);
                            } else {
                                return String.valueOf(formulaValue);
                            }
                        case BOOLEAN:
                            return String.valueOf(cell.getBooleanCellValue());
                        default:
                            return cell.getCellFormula();
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return null;
        }
    }
}
