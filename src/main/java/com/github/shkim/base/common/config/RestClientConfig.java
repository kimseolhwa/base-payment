package com.github.shkim.base.common.config;

import com.github.shkim.base.common.interceptor.RestClientLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 외부 API 연동을 위한 RestClient 공통 설정 클래스.
 * <p>
 * Apache HttpClient5 기반의 커넥션 풀 적용, 타임아웃 설정 및 
 * 외부 통신 로깅/마스킹 처리를 위한 인터셉터 주입 수행
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
     * 커넥션 풀을 적용하여 TCP 핸드셰이크 오버헤드를 줄이고,
     * 로깅 인터셉터에서 스트림(InputStream)을 읽은 후에도 실제 비즈니스 로직에서
     * 데이터를 다시 읽을 수 있도록 Buffering 래퍼 적용
     * </p>
     *
     * @return RestClient 객체
     */
    @Bean
    public RestClient customRestClient() {
        
        // 1. 커넥션 풀 매니저 설정
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // 전체 최대 커넥션 수
        connectionManager.setDefaultMaxPerRoute(20); // IP/도메인(라우트)당 최대 커넥션 수

        // 2. 요청 타임아웃 설정
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(3)) // 커넥션 풀에서 커넥션을 가져오기까지 대기 시간 (3초)
                .setResponseTimeout(Timeout.ofSeconds(5))          // 데이터 수신 타임아웃 (Read Timeout, 5초)
                // Connect Timeout 설정은 HttpClient 생성 시점에 설정 불가. Spring6/HttpClient5 에서는 ConnectionConfig 등으로 설정하지만 여기서는 기본값 사용
                .build();

        // 3. HttpClient 생성
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 4. HttpComponentsClientHttpRequestFactory 로 래핑하여 스프링에 연결
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // ConnectTimeout을 위해 추가 설정 가능 (선택적)
        factory.setConnectTimeout(3000); 
        
        // 5. RestClient 빌드 및 설정
        return RestClient.builder()
                // [핵심] 응답 스트림(InputStream)을 한 번 읽어도 날아가지 않게 메모리에 버퍼링해 주는 팩토리로 감싸기
                .requestFactory(new BufferingClientHttpRequestFactory(factory))
                // 외부 API 통신 직전/직후를 가로채는 인터셉터 등록
                .requestInterceptor(loggingInterceptor)
                .build();
    }
}