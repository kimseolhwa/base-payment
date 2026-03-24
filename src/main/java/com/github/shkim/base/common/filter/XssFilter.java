package com.github.shkim.base.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 일반 폼(Form) 및 쿼리 파라미터 XSS 방어를 위한 서블릿 필터.
 * <p>
 * 클라이언트의 원본 요청(Request)을 XssRequestWrapper로 감싸서
 * 컨트롤러 도달 전 악성 스크립트 필터링 수행
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter implements Filter {

    /**
     * 원본 Request 객체를 XSS 방어용 래퍼로 교체하여 체인으로 전달
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // H2 콘솔 경로는 XSS 래핑을 건너뜀 (내부 통신 스크립트 변조 방지)
        if (httpRequest.getRequestURI().startsWith("/h2-console")) {
            chain.doFilter(request, response);
            return;
        }

        // 들어온 원본 요청(Request)에 우리가 만든 XSS 방어용 래퍼를 씌움
        XssRequestWrapper xssWrapper = new XssRequestWrapper(httpRequest);

        // 래퍼가 씌워진 안전한 요청을 다음 필터(또는 컨트롤러)로 전달!
        chain.doFilter(xssWrapper, response);
    }
}