package com.quiz.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 *
 * @author Quiz System
 * @since 2024-09-26
 */
public interface FileUploadService {

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 上传题库文件
     */
    String uploadQuestionFile(MultipartFile file, Long userId);

    /**
     * 上传普通文件
     */
    String uploadFile(MultipartFile file, Long userId, String category);

    /**
     * 删除文件
     */
    boolean deleteFile(String fileUrl);

    /**
     * 获取文件完整URL
     */
    String getFullUrl(String relativePath);

    /**
     * 验证文件类型
     */
    boolean validateFileType(MultipartFile file, String[] allowedTypes);

    /**
     * 验证文件大小
     */
    boolean validateFileSize(MultipartFile file, long maxSizeInMB);
}