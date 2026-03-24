package com.github.shkim.base.common.exception;

/**
 * DB 조회 결과가 존재하지 않거나 유효하지 않을 때 발생하는 런타임 예외 클래스.
 * <p>
 * 비즈니스 로직에서 발생 시 GlobalExceptionHandler가 캐치하여
 * 400 Bad Request 및 표준 JSON/HTML 에러 응답으로 변환 처리 수행
 * </p>
 */
public class DataNotFoundException extends RuntimeException {

    /**
     * 상세 에러 메시지를 포함하는 예외 객체 생성 수행
     *
     * @param message 클라이언트에게 전달할 비즈니스 에러 메시지
     */
    public DataNotFoundException(String message) {
        super(message);
    }
}