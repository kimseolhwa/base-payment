package com.github.shkim.base.common.exception;

/**
 * 처리율 제한(Rate Limit) 초과 시 발생하는 예외
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}