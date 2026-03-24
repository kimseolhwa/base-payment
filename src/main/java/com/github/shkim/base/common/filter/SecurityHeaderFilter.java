package com.github.shkim.base.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 웹 취약점 방어를 위한 HTTP 보안 헤더 전역 주입 필터.
 * <p>
 * 모든 HTTP 응답 헤더에 XSS 보호, 클릭재킹 방지, MIME 스니핑 차단, HSTS, CSP 정책을 강제 삽입하여
 * 클라이언트 측 보안 취약점 사전 차단 수행
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행
public class SecurityHeaderFilter implements Filter {

    /**
     * 서블릿 응답 객체를 가로채어 표준 보안 헤더 세팅 수행
     *
     * @param request 클라이언트 HTTP 요청
     * @param response 서버 HTTP 응답
     * @param chain 다음 필터 처리를 위한 필터 체인
     * @throws IOException 입출력 처리 중 오류 발생 시
     * @throws ServletException 서블릿 처리 중 오류 발생 시
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // H2 콘솔 경로는 보안 헤더 주입을 건너뜀 (프레임 및 스크립트 허용)
        if (httpRequest.getRequestURI().startsWith("/h2-console")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 브라우저가 MIME 타입을 임의로 추측하는 것을 방지
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        // iframe 내에 현재 페이지가 렌더링되는 것을 방지 (클릭재킹 방어)
        httpResponse.setHeader("X-Frame-Options", "DENY");
        // 브라우저 내장 XSS 필터 강제 활성화
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        // HTTPS 통신 강제 (1년 유지)
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        // 허용된 출처(자신의 도메인)의 리소스만 로드 허용
        httpResponse.setHeader("Content-Security-Policy", "default-src 'self'");

        chain.doFilter(request, response);
    }
}