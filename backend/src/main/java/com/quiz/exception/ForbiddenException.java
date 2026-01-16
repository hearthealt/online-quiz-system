package com.quiz.exception;

/**
 * 权限不足异常
 *
 * @author Quiz System
 * @since 2024-10-09
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}