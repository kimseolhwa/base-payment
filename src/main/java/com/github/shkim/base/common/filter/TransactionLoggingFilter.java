package com.github.shkim.base.common.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shkim.base.common.util.MaskingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 전 구간 트랜잭션 추적 및 API 요청/응답 로깅을 담당하는 최상단 서블릿 필터.
 * <p>
 * 들어오는 모든 HTTP 요청에 대해 고유한 추적 식별자(trace_id)를 발급하거나 유지하며,
 * 요청(Request) 및 응답(Response)의 본문(Body)을 가로채어 개인정보 마스킹 처리 후 로그로 출력
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2) // 필터순서 : Security -> XssFilter -> TransactionLoggingFilter
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_KEY = "trace_id"; // MDC 키 통일
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 특정 경로는 필터를 타지 않도록 제외 처리 수행.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // H2 콘솔 및 정적 리소스(파비콘 등)는 무거운 로깅과 Body 캐싱을 하지 않음
        return path.startsWith("/h2-console") || path.startsWith("/favicon.ico");
    }

    /**
     * 필터 체인의 핵심 로직을 수행
     *
     * @param request  클라이언트의 HTTP 요청 객체
     * @param response 서버의 HTTP 응답 객체
     * @param filterChain 다음 필터로 요청을 전달하기 위한 체인 객체
     * @throws ServletException 서블릿 처리 중 오류 발생 시
     * @throws IOException 입출력 처리 중 오류 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 래퍼 적용 (Body 미리 읽기)
        CachedBodyHttpServletRequest cachingRequest = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        // 2. TRACE_ID 획득 또는 생성
        String traceId = resolveOrGenerateTraceId(cachingRequest);
        MDC.put(TRACE_ID_KEY, traceId);

        // 3. 응답 헤더에 TRACE_ID를 심어서 클라이언트가 알 수 있게 함 (선택적 편의 기능)
        response.setHeader("X-Trace-Id", traceId);

        String clientIp = getClientIp(request);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 필터 체인 진행 (이후 컨트롤러 실행)
            filterChain.doFilter(cachingRequest, cachingResponse);
        } finally {
            stopWatch.stop();

            // 4. Request / Response 로깅
            String rawRequestBody = new String(cachingRequest.getCachedBody(), StandardCharsets.UTF_8);
            String maskedRequestBody = getMaskedJsonPayload(rawRequestBody);

            log.info("[REQUEST] IP: {} | URI: {} | Method: {} | Payload: {}",
                    clientIp, request.getRequestURI(), request.getMethod(),
                    !StringUtils.hasText(maskedRequestBody) ? request.getQueryString() : maskedRequestBody);

            String rawResponseBody = new String(cachingResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            String maskedResponseBody = getMaskedJsonPayload(rawResponseBody);

            log.info("[RESPONSE] Status: {} | Time: {}ms | Payload: {}",
                    response.getStatus(), stopWatch.getTotalTimeMillis(), maskedResponseBody);

            cachingResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    /**
     * 기존 요청에서 전달된 trace_id가 있는지 확인하고, 없을 경우 merchantId 기반으로 새 식별자 생성 수행.
     *
     * @param request 캐싱된 HTTP 요청 래퍼 객체
     * @return 10자리의 고유 트랜잭션 식별자 (trace_id)
     */
    private String resolveOrGenerateTraceId(CachedBodyHttpServletRequest request) {
        // Header 또는 파라미터로 넘겨준 기존 trace_id가 있는지 확인
        String incomingTraceId = Optional.ofNullable(request.getHeader("X-Trace-Id"))
                .filter(StringUtils::hasText)
                .orElseGet(() -> request.getParameter("trace_id"));

        // 둘 중 하나라도 유효한 값이 있다면 그대로 반환
        if (StringUtils.hasText(incomingTraceId)) {
            return incomingTraceId;
        }

        // 없다면 신규 생성
        String merchantId = extractMerchantId(request);
        String random5 = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();

        if (!StringUtils.hasText(merchantId)) {
            // merchantId 없으면 랜덤 10자리
            return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } else {
            // merchantId 있으면: merchantId 5자리(부족하면 0으로 패딩) + 랜덤 5자리 = 총 10자리
            String prefix = merchantId.trim();
            if (prefix.length() < 5) {
                prefix = String.format("%-5s", prefix).replace(' ', '0');
            } else {
                prefix = prefix.substring(0, 5);
            }
            return prefix.toUpperCase() + random5;
        }
    }

    /**
     * 쿼리 파라미터 또는 JSON Body에서 가맹점 ID(merchantId) 추출
     *
     * @param request 캐싱된 HTTP 요청 래퍼 객체
     * @return 추출된 가맹점 ID (없을 경우 null 반환)
     */
    private String extractMerchantId(CachedBodyHttpServletRequest request) {
        String merchantId = request.getParameter("merchantId");
        if (!StringUtils.hasText(merchantId)) merchantId = request.getParameter("merchant_id");;

        if (!StringUtils.hasText(merchantId) && request.getContentType() != null && request.getContentType().contains("application/json")) {
            try {
                String body = new String(request.getCachedBody(), StandardCharsets.UTF_8);
                if (StringUtils.hasText(body)) {
                    JsonNode node = objectMapper.readTree(body);
                    if (node.has("merchant_id")) return node.get("merchant_id").asText();
                    if (node.has("merchantId")) return node.get("merchantId").asText();
                }
            } catch (Exception e) {
                log.warn("[Trace ID] JSON Body 파싱 실패. 랜덤 아이디로 대체합니다.");
            }
        }
        return merchantId;
    }

    /**
     * JSON 형태의 페이로드를 파싱하여 민감 정보를 마스킹 처리한 문자열로 반환
     *
     * @param rawPayload 원본 페이로드 문자열
     * @return 마스킹 처리된 JSON 문자열 (파싱 실패 시 단순 문자열 치환 수행)
     */
    private String getMaskedJsonPayload(String rawPayload) {
        if (!StringUtils.hasText(rawPayload)) return rawPayload;
        try {
            Map<String, Object> map = objectMapper.readValue(rawPayload, new TypeReference<>() {});
            return objectMapper.writeValueAsString(MaskingUtil.logMasking(map));
        } catch (Exception e) {
            return MaskingUtil.logMasking(rawPayload, "&");
        }
    }

    /**
     * L4, 프록시 등 우회 환경을 고려하여 클라이언트의 실제 IP 주소를 추출
     *
     * @param request HTTP 요청 객체
     * @return 클라이언트 IP 주소
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}