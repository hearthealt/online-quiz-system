package com.quiz.exception;

/**
 * 资源未找到异常
 *
 * @author Quiz System
 * @since 2024-10-09
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}