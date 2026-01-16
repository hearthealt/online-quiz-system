package com.quiz.controller;

import com.quiz.dto.Result;
import com.quiz.service.FileUploadService;
import com.quiz.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            if (file.isEmpty()) {
                return Result.badRequest("请选择要上传的头像文件");
            }

            String relativePath = fileUploadService.uploadAvatar(file, userId);
            String fullUrl = fileUploadService.getFullUrl(relativePath);

            Map<String, String> result = new HashMap<>();
            result.put("url", fullUrl);
            result.put("relativePath", relativePath);
            result.put("fileName", file.getOriginalFilename());

            return Result.success("头像上传成功", result);
        } catch (Exception e) {
            log.error("上传头像失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传题库文件
     */
    @PostMapping("/questions")
    public Result<Map<String, String>> uploadQuestionFile(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            if (file.isEmpty()) {
                return Result.badRequest("请选择要上传的题库文件");
            }

            String relativePath = fileUploadService.uploadQuestionFile(file, userId);
            String fullUrl = fileUploadService.getFullUrl(relativePath);

            Map<String, String> result = new HashMap<>();
            result.put("url", fullUrl);
            result.put("relativePath", relativePath);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", String.valueOf(file.getSize()));

            return Result.success("题库文件上传成功", result);
        } catch (Exception e) {
            log.error("上传题库文件失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传普通文件
     */
    @PostMapping("/file")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            if (file.isEmpty()) {
                return Result.badRequest("请选择要上传的文件");
            }

            String relativePath = fileUploadService.uploadFile(file, userId, category);
            String fullUrl = fileUploadService.getFullUrl(relativePath);

            Map<String, String> result = new HashMap<>();
            result.put("url", fullUrl);
            result.put("relativePath", relativePath);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", String.valueOf(file.getSize()));
            result.put("category", category);

            return Result.success("文件上传成功", result);
        } catch (Exception e) {
            log.error("上传文件失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> uploadBatchFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            if (files == null || files.length == 0) {
                return Result.badRequest("请选择要上传的文件");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("files", new java.util.ArrayList<>());

            for (MultipartFile file : files) {
                try {
                    if (!file.isEmpty()) {
                        String relativePath = fileUploadService.uploadFile(file, userId, category);
                        String fullUrl = fileUploadService.getFullUrl(relativePath);

                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("url", fullUrl);
                        fileInfo.put("relativePath", relativePath);
                        fileInfo.put("fileName", file.getOriginalFilename());
                        fileInfo.put("status", "success");

                        ((java.util.List<Map<String, String>>) result.get("files")).add(fileInfo);
                        result.put("successCount", (Integer) result.get("successCount") + 1);
                    }
                } catch (Exception e) {
                    Map<String, String> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", file.getOriginalFilename());
                    fileInfo.put("error", e.getMessage());
                    fileInfo.put("status", "failed");

                    ((java.util.List<Map<String, String>>) result.get("files")).add(fileInfo);
                    result.put("failCount", (Integer) result.get("failCount") + 1);
                }
            }

            return Result.success("批量上传完成", result);
        } catch (Exception e) {
            log.error("批量上传文件失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<Void> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            boolean deleted = fileUploadService.deleteFile(fileUrl);
            if (deleted) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败或文件不存在");
            }
        } catch (Exception e) {
            log.error("删除文件失败: ", e);
            return Result.error(e.getMessage());
        }
    }
}