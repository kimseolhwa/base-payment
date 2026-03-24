package com.github.shkim.base.common.exception;

/**
 * 멱등성 키 검증 실패 또는 동시 중복 요청 처리 중 발생하는 런타임 예외.
 */
public class IdempotencyException extends RuntimeException {

    /**
     * 상세 에러 메시지를 포함하는 예외 객체 생성 수행
     *
     * @param message 예외 상세 사유
     */
    public IdempotencyException(String message) {
        super(message);
    }
}