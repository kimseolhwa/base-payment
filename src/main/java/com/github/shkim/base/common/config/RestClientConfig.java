package com.github.shkim.base.common.config;

import com.github.shkim.base.common.interceptor.RestClientLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 외부 API 연동을 위한 RestClient 공통 설정 클래스.
 * <p>
 * 타임아웃 설정 및 외부 통신 로깅/마스킹 처리를 위한 인터셉터 주입 수행
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    // 직접 만든 외부 통신 공통 로깅 & 마스킹 인터셉터 주입
    private final RestClientLoggingInterceptor loggingInterceptor;

    /**
     * 공통 설정이 적용된 RestClient 빈(Bean) 생성 및 반환.
     * <p>
     * 로깅 인터셉터에서 스트림(InputStream)을 읽은 후에도 실제 비즈니스 로직에서
     * 데이터를 다시 읽을 수 있도록 Buffering 래퍼 적용
     * </p>
     *
     * @return RestClient 객체
     */
    @Bean
    public RestClient customRestClient() {
        // 1. 타임아웃 설정을 위한 기본 HTTP 요청 팩토리 생성
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 서버 연결 타임아웃 (3초)
        factory.setReadTimeout(5000);    // 데이터 수신 타임아웃 (5초)

        // 2. RestClient 빌드 및 설정
        return RestClient.builder()
                // [핵심] 응답 스트림(InputStream)을 한 번 읽어도 날아가지 않게 메모리에 버퍼링해 주는 팩토리로 감싸기
                // 이 설정이 없으면 Interceptor에서 로그를 찍기 위해 응답 바디를 읽는 순간, 실제 비즈니스 로직에서는 데이터가 비어버림
                .requestFactory(new BufferingClientHttpRequestFactory(factory))
                // 외부 API 통신 직전/직후를 가로채는 인터셉터 등록
                .requestInterceptor(loggingInterceptor)
                .build();
    }
}