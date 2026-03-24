package com.github.shkim.base.common.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * HTTP 요청 파라미터 XSS 필터링을 위한 커스텀 래퍼 클래스.
 * <p>
 * Jsoup 라이브러리를 활용하여 파라미터 및 헤더 값에 포함된 HTML 태그 완벽 제거 수행
 * </p>
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * Jsoup을 활용한 단일 문자열 내 악성 HTML 태그 제거 로직
     *
     * @param value 클라이언트가 전달한 원본 문자열
     * @return HTML 태그가 모두 제거된 안전한 문자열
     */
    private String cleanXSS(String value) {
        if (!StringUtils.hasText(value)) return value;
        // Safelist.none() : 모든 HTML 태그를 허용하지 않고 텍스트만 남김 (<script> 등 완벽 차단)
        return Jsoup.clean(value, Safelist.none());
    }

    /**
     * 배열 형태의 요청 파라미터 전체에 대한 XSS 필터링 수행
     *
     * @param parameter 파라미터 키
     * @return 필터링이 완료된 파라미터 값 배열
     */
    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;

        // Stream을 사용하여 각 요소에 cleanXSS를 적용하고 다시 배열로 변환
        return Arrays.stream(values)
                .map(this::cleanXSS)
                .toArray(String[]::new);
    }

    /**
     * 단일 요청 파라미터에 대한 XSS 필터링 수행
     */
    @Override
    public String getParameter(String parameter) {
        return cleanXSS(super.getParameter(parameter));
    }

    /**
     * 요청 HTTP 헤더 값에 대한 XSS 필터링 수행
     */
    @Override
    public String getHeader(String name) {
        return cleanXSS(super.getHeader(name));
    }
}