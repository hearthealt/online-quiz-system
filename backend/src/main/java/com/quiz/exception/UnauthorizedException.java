package com.quiz.exception;

/**
 * 未授权异常
 *
 * @author Quiz System
 * @since 2024-10-09
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}