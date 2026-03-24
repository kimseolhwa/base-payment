package com.github.shkim.base.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 멱등성(Idempotency) 보장을 위한 커스텀 애노테이션.
 * <p>
 * 결제 승인, 노티 발송 등 중복 실행 시 치명적인 부작용이 발생하는
 * 쓰기(Write) 로직 컨트롤러 메서드에 부여하여 중복 요청 차단 수행
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
}