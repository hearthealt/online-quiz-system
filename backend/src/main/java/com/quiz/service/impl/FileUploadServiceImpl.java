package com.quiz.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.quiz.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

/**
 * 文件上传服务实现类
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:/tmp/quiz-uploads}")
    private String uploadPath;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:9090}")
    private String serverPort;

    @Value("${server.host:45.192.104.123}")
    private String serverHost;

    @Value("${file.base-url:}")
    private String fileBaseUrl;

    // 头像允许的文件类型
    private static final String[] AVATAR_ALLOWED_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    // 题库文件允许的类型
    private static final String[] QUESTION_FILE_ALLOWED_TYPES = {
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    // 普通文件允许的类型
    private static final String[] COMMON_FILE_ALLOWED_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "text/plain", "application/json",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "text/csv"
    };

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            // 验证文件类型
            if (!validateFileType(file, AVATAR_ALLOWED_TYPES)) {
                throw new RuntimeException("不支持的头像文件类型");
            }

            // 验证文件大小 (5MB)
            if (!validateFileSize(file, 5)) {
                throw new RuntimeException("头像文件大小不能超过5MB");
            }

            return uploadFile(file, userId, "avatar");
        } catch (Exception e) {
            log.error("上传头像失败: ", e);
            throw new RuntimeException("上传头像失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadQuestionFile(MultipartFile file, Long userId) {
        try {
            // 验证文件类型
            if (!validateFileType(file, QUESTION_FILE_ALLOWED_TYPES)) {
                throw new RuntimeException("不支持的题库文件类型，仅支持Excel格式（.xlsx, .xls）");
            }

            // 验证文件大小 (10MB)
            if (!validateFileSize(file, 10)) {
                throw new RuntimeException("题库文件大小不能超过10MB");
            }

            return uploadFile(file, userId, "questions");
        } catch (Exception e) {
            log.error("上传题库文件失败: ", e);
            throw new RuntimeException("上传题库文件失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, Long userId, String category) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("上传文件不能为空");
            }

            // 验证通用文件类型
            if (!validateFileType(file, COMMON_FILE_ALLOWED_TYPES)) {
                throw new RuntimeException("不支持的文件类型");
            }

            // 创建上传目录结构：uploadPath/category/yyyy/MM/dd/
            String dateDir = DateUtil.format(new Date(), "yyyy/MM/dd");
            String categoryDir = StrUtil.isNotBlank(category) ? category : "common";
            String dirPath = uploadPath + File.separator + categoryDir + File.separator + dateDir;

            File directory = new File(dirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = IdUtil.simpleUUID() + fileExtension;
            String filePath = dirPath + File.separator + fileName;

            // 保存文件
            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path);

            // 返回相对路径
            String relativePath = "/" + categoryDir + "/" + dateDir + "/" + fileName;

            log.info("文件上传成功: userId={}, category={}, fileName={}, path={}",
                    userId, category, fileName, relativePath);

            return relativePath;
        } catch (IOException e) {
            log.error("文件上传失败: ", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (StrUtil.isBlank(fileUrl)) {
                return false;
            }

            // 从URL中提取相对路径
            String relativePath = fileUrl;
            if (fileUrl.startsWith("http")) {
                relativePath = fileUrl.substring(fileUrl.lastIndexOf("/uploads"));
            }

            String fullPath = uploadPath + relativePath.replace("/uploads", "");
            File file = new File(fullPath);

            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("删除文件: path={}, success={}", fullPath, deleted);
                return deleted;
            }

            return false;
        } catch (Exception e) {
            log.error("删除文件失败: ", e);
            return false;
        }
    }

    @Override
    public String getFullUrl(String relativePath) {
        if (StrUtil.isBlank(relativePath)) {
            return "";
        }

        // 如果已经是完整URL，直接返回
        if (relativePath.startsWith("http")) {
            return relativePath;
        }

        // 规范化相对路径前缀
        String normalizedRelative = relativePath.startsWith("/") ? relativePath : ("/" + relativePath);

        // 如果配置了对外访问域名或CDN前缀，优先使用（支持HTTPS）
        if (StrUtil.isNotBlank(fileBaseUrl)) {
            String base = fileBaseUrl.endsWith("/") ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) : fileBaseUrl;
            String ctx = StrUtil.isBlank(contextPath) ? "" : (contextPath.startsWith("/") ? contextPath : ("/" + contextPath));
            return String.format("%s%s/uploads%s", base, ctx, normalizedRelative);
        }

        // 回退到基于host:port的HTTP形式
        return String.format("http://%s:%s%s/uploads%s", serverHost, serverPort, contextPath, normalizedRelative);
    }

    @Override
    public boolean validateFileType(MultipartFile file, String[] allowedTypes) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (StrUtil.isBlank(contentType)) {
            return false;
        }

        return Arrays.asList(allowedTypes).contains(contentType.toLowerCase());
    }

    @Override
    public boolean validateFileSize(MultipartFile file, long maxSizeInMB) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        long fileSizeInMB = file.getSize() / (1024 * 1024);
        return fileSizeInMB <= maxSizeInMB;
    }
}