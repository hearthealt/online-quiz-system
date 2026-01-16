package com.quiz.controller;

import com.quiz.dto.Result;
import com.quiz.dto.QuestionImportResult;
import com.quiz.entity.QuestionBank;
import com.quiz.entity.Question;
import com.quiz.service.QuestionBankService;
import com.quiz.service.QuestionService;
import com.quiz.service.QuestionImportService;
import com.quiz.service.FileUploadService;
import com.quiz.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题库控制器
 *
 * @author Quiz System
 * @since 2024
 */
@RestController
@RequestMapping("/banks")
@RequiredArgsConstructor
@Tag(name = "题库管理", description = "题库的增删改查、导入等接口")
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final QuestionService questionService;
    private final QuestionImportService questionImportService;
    private final FileUploadService fileUploadService;

    /**
     * 获取所有启用的题库列表（带用户进度）
     */
    @Operation(summary = "获取题库列表", description = "获取所有启用的题库，包含用户的练习/考试进度")
    @GetMapping
    public Result<List<QuestionBank>> getBanks() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<QuestionBank> banks = questionBankService.getEnabledBanksWithProgress(userId);
        return Result.success(banks);
    }

    /**
     * 获取题库详情
     */
    @Operation(summary = "获取题库详情")
    @GetMapping("/{bankId}")
    public Result<QuestionBank> getBankDetail(@PathVariable Long bankId) {
        QuestionBank bank = questionBankService.getById(bankId);
        if (bank == null) {
            return Result.error("题库不存在");
        }
        return Result.success(bank);
    }

    /**
     * 获取题库下的所有题目（按顺序）
     */
    @Operation(summary = "获取题库题目", description = "获取指定题库下的所有题目，按顺序排列")
    @GetMapping("/{bankId}/questions")
    public Result<List<Question>> getBankQuestions(@PathVariable Long bankId) {
        QuestionBank bank = questionBankService.getById(bankId);
        if (bank == null) {
            return Result.error("题库不存在");
        }
        List<Question> questions = questionService.getQuestionsByBankId(bankId);
        return Result.success(questions);
    }

    /**
     * 检查题库是否存在
     */
    @Operation(summary = "检查题库是否存在", description = "根据名称检查题库是否已存在")
    @GetMapping("/check")
    public Result<Map<String, Object>> checkBankExists(@RequestParam String name) {
        Map<String, Object> result = new HashMap<>();
        QuestionBank bank = questionBankService.getByName(name);
        result.put("exists", bank != null);
        result.put("bank", bank);
        return Result.success(result);
    }

    /**
     * 上传文件创建/更新题库
     * 文件名作为题库名称
     */
    @Operation(summary = "导入题库", description = "上传Excel文件，文件名作为题库名称")
    @PostMapping("/import")
    public Result<Map<String, Object>> importBank(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "overwrite") String mode) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 获取文件名作为题库名称
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名不能为空");
            }

            // 去除扩展名得到题库名称
            String bankName = originalFilename;
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                bankName = originalFilename.substring(0, lastDotIndex);
            }

            // 检查文件名是否包含非法字符
            if (bankName.matches(".*[\\\\/:*?\"<>|].*")) {
                return Result.error("题库名称不能包含特殊字符: \\ / : * ? \" < > |");
            }

            // 检查文件格式
            if (!questionImportService.isFormatSupported(originalFilename)) {
                return Result.error("不支持的文件格式，支持的格式：" +
                    String.join(", ", questionImportService.getSupportedFormats()));
            }

            // 上传文件
            String filePath = fileUploadService.uploadQuestionFile(file, userId);
            if (filePath == null || filePath.isEmpty()) {
                return Result.error("文件上传失败");
            }

            // 检查题库是否存在
            QuestionBank existingBank = questionBankService.getByName(bankName);
            boolean isNewBank = existingBank == null;
            QuestionBank bank;

            if (isNewBank) {
                // 创建新题库
                bank = questionBankService.createBank(bankName, userId);
            } else {
                bank = existingBank;
                // 如果是覆盖模式，删除旧题目
                if ("overwrite".equals(mode)) {
                    questionService.deleteByBankId(bank.getId());
                }
            }

            // 导入题目
            QuestionImportResult importResult = questionImportService.importQuestions(
                filePath, originalFilename, bank.getId(), userId);

            // 更新题库的题目数量
            questionBankService.updateQuestionCount(bank.getId());

            // 重新获取题库信息
            bank = questionBankService.getById(bank.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("bank", bank);
            result.put("importResult", importResult);
            result.put("importedCount", importResult.getSuccessCount());
            result.put("totalCount", bank.getQuestionCount());
            result.put("isNewBank", isNewBank);

            if (importResult.getSuccessCount() > 0) {
                return Result.success("导入成功", result);
            } else {
                return Result.error("导入失败: " + importResult.getMessage());
            }
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 创建空题库
     */
    @Operation(summary = "创建题库", description = "创建一个空的题库")
    @PostMapping
    public Result<QuestionBank> createBank(@RequestBody Map<String, Object> params) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            String name = (String) params.get("name");
            if (name == null || name.trim().isEmpty()) {
                return Result.error("题库名称不能为空");
            }

            // 检查是否已存在
            if (questionBankService.existsByName(name)) {
                return Result.error("题库名称已存在");
            }

            QuestionBank bank = questionBankService.createBank(name.trim(), userId);

            // 设置描述
            String description = (String) params.get("description");
            if (description != null && !description.trim().isEmpty()) {
                questionBankService.updateBankInfo(bank.getId(), description.trim(), null, null);
                bank = questionBankService.getById(bank.getId());
            }

            return Result.success("创建成功", bank);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新题库信息
     */
    @Operation(summary = "更新题库信息", description = "更新题库描述、状态、排序（不能修改名称）")
    @PutMapping("/{bankId}")
    public Result<QuestionBank> updateBank(
            @PathVariable Long bankId,
            @RequestBody Map<String, Object> params) {
        try {
            String description = (String) params.get("description");
            Integer status = params.get("status") != null ?
                Integer.parseInt(params.get("status").toString()) : null;
            Integer sortOrder = params.get("sortOrder") != null ?
                Integer.parseInt(params.get("sortOrder").toString()) : null;

            boolean success = questionBankService.updateBankInfo(bankId, description, status, sortOrder);
            if (success) {
                QuestionBank bank = questionBankService.getById(bankId);
                return Result.success("更新成功", bank);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 导入题目到指定题库
     */
    @Operation(summary = "导入题目到指定题库", description = "上传Excel文件导入题目到指定题库")
    @PostMapping("/{bankId}/import")
    public Result<Map<String, Object>> importToBank(
            @PathVariable Long bankId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "append") String mode) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 检查题库是否存在
            QuestionBank bank = questionBankService.getById(bankId);
            if (bank == null) {
                return Result.error("题库不存在");
            }

            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名不能为空");
            }

            // 检查文件格式
            if (!questionImportService.isFormatSupported(originalFilename)) {
                return Result.error("不支持的文件格式，支持的格式：" +
                    String.join(", ", questionImportService.getSupportedFormats()));
            }

            // 上传文件
            String filePath = fileUploadService.uploadQuestionFile(file, userId);
            if (filePath == null || filePath.isEmpty()) {
                return Result.error("文件上传失败");
            }

            // 如果是覆盖模式，删除旧题目
            if ("overwrite".equals(mode)) {
                questionService.deleteByBankId(bank.getId());
            }

            // 导入题目
            QuestionImportResult importResult = questionImportService.importQuestions(
                filePath, originalFilename, bank.getId(), userId);

            // 更新题库的题目数量
            questionBankService.updateQuestionCount(bank.getId());

            // 重新获取题库信息
            bank = questionBankService.getById(bank.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("bank", bank);
            result.put("importResult", importResult);
            result.put("importedCount", importResult.getSuccessCount());
            result.put("totalCount", bank.getQuestionCount());

            if (importResult.getSuccessCount() > 0) {
                return Result.success("导入成功", result);
            } else {
                return Result.error("导入失败: " + importResult.getMessage());
            }
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取题库统计信息
     */
    @Operation(summary = "获取题库统计", description = "获取题库下各类题型数量统计")
    @GetMapping("/{bankId}/stats")
    public Result<Map<String, Object>> getBankStats(@PathVariable Long bankId) {
        QuestionBank bank = questionBankService.getById(bankId);
        if (bank == null) {
            return Result.error("题库不存在");
        }

        Map<String, Object> stats = questionService.getQuestionStatsByBankId(bankId);
        return Result.success(stats);
    }

    /**
     * 删除题库
     */
    @Operation(summary = "删除题库", description = "删除题库及其所有题目")
    @DeleteMapping("/{bankId}")
    public Result<Void> deleteBank(@PathVariable Long bankId) {
        try {
            boolean success = questionBankService.deleteBank(bankId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
