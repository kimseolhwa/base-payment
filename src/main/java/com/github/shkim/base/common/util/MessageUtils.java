package com.github.shkim.base.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 다국어 메시지 처리를 위한 공통 유틸리티 클래스.
 * <p>
 * Spring의 MessageSource를 활용하여 클라이언트의 로케일(Locale)에 맞는
 * 시스템 에러 메시지 및 안내 문구 추출 수행
 * </p>
 */
@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    /**
     * 메시지 코드를 기반으로 현재 로케일에 맞는 다국어 텍스트 반환.
     *
     * @param code messages.properties에 정의된 키 (예: error.notfound)
     * @return 로케일이 적용된 다국어 문자열
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * 동적 파라미터가 포함된 다국어 텍스트를 파싱하여 반환.
     *
     * @param code 다국어 메시지 키
     * @param args 메시지 내 치환자({0}, {1} 등)에 바인딩할 객체 배열
     * @return 치환 및 로케일이 적용된 최종 다국어 문자열
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}