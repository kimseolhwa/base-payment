package com.github.shkim.base.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전역 API 응답 코드 및 메시지 키를 정의하는 Enum 클래스.
 * <p>
 * 비즈니스 로직 성공/실패 상태를 표준화하고, 메시지는 properties 파일을 통해 다국어 처리
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    SUCCESS("0000", "response.success"),
    BAD_REQUEST("4000", "response.bad_request"),
    VALIDATION_ERROR("4001", "response.validation_error"),
    CONFLICT("4090", "response.conflict"),
    INTERNAL_SERVER_ERROR("9999", "response.server_error");

    private final String code;
    private final String messageKey;
}