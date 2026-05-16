package com.github.shkim.base.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 외부 가맹점 API 연동 시 Authorization 헤더를 통해 인증을 수행하는 커스텀 필터.
 * <p>
 * /api/** 경로로 들어오는 요청에 대해 가맹점의 Secret Key를 검증합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    // 향후 실제 DB나 캐시에서 검증 로직을 분리하기 위해 Service 객체를 주입받는 것을 권장합니다.
    // 여기서는 예시로 하드코딩된 SecretKey(TEST_M001 가맹점)를 대조합니다.
    private static final String MOCK_SECRET_KEY = "sec_test_abc123"; 

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. /api/로 시작하지 않거나 Swagger/상태 체크 등은 검증 제외
        if (!requestURI.startsWith("/api/") || requestURI.contains("/swagger-ui") || requestURI.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Authorization 헤더 추출 (예: Authorization: Bearer sec_test_abc123)
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("[Authentication] 인증 헤더 누락 또는 형식 오류. URI: {}", requestURI);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증 정보가 누락되었습니다. (Authorization: Bearer {secretKey})");
            return;
        }

        // 3. 토큰(Secret Key) 검증
        String token = authHeader.substring(7);
        // TODO: 실제 환경에서는 DB의 merchant_info 테이블에서 token을 조회하여 검증하는 로직(MerchantService 등) 연동
        if (!MOCK_SECRET_KEY.equals(token)) {
            log.warn("[Authentication] 유효하지 않은 API 키 접근. Token: {}", token);
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "유효하지 않은 가맹점 인증 키입니다.");
            return;
        }

        // 인증 성공 시 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 인증 실패 시 JSON 형식으로 에러 응답 반환
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("res_cd", "4010"); // 커스텀 인증 에러 코드
        errorBody.put("res_msg", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}