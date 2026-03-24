package com.github.shkim.base.common.web;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * HTML View(Thymeleaf) 렌더링 시 전역 Model 데이터를 자동 주입하는 컨트롤러 어드바이스.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    /**
     * 현재 스레드 컨텍스트(MDC)에 저장된 트랜잭션 식별자(trace_id)를 추출하여 Model에 바인딩.
     * <p>
     * 브라우저에서 후속 요청 시 동일한 식별자를 유지하기 위함
     * </p>
     *
     * @return 트랜잭션 추적 번호
     */
    @ModelAttribute("trace_id")
    public String addTraceIdToModel() {
        return MDC.get("trace_id");
    }
}