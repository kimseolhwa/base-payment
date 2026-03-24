package com.github.shkim.base.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전역 Web MVC 설정 클래스.
 * <p>
 * 프론트엔드 연동을 위한 CORS 정책 정의 및 JSON XSS 방어용 커스텀 컨버터 등록 수행
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    /**
     * API 접근에 대한 CORS(교차 출처 리소스 공유) 매핑 정책 설정.
     * <p>
     * 도메인에 대한 명시적 허용 및 인증 정보(Credentials) 포함 허용
     * </p>
     *
     * @param registry CORS 설정을 등록할 레지스트리 객체
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // API 경로에 대해서만
                .allowedOrigins(allowedOrigins) // 설정파일에 정의된 도메인만 허용 (절대 '*' 금지)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 쿠키/인증 정보 포함 허용
                .maxAge(3600); // Preflight 요청 캐싱 시간(1시간)
    }

    /**
     * XSS 방어 로직이 적용된 Jackson HTTP 메시지 컨버터 빈(Bean) 생성 및 반환.
     * <p>
     * API 응답 시 객체를 JSON으로 직렬화하는 과정에서 HtmlCharacterEscapes를 거치도록 설정하여
     * 전역적인 JSON 응답 보안 강화 수행
     * </p>
     *
     * @param builder 스프링 부트 자동 구성에 의해 주입된 Jackson 빌더
     * @return 커스텀 이스케이프 정책이 장착된 메시지 컨버터
     */
    @Bean
    public MappingJackson2HttpMessageConverter jsonEscapeConverter(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        objectMapper.getFactory().setCharacterEscapes(new HtmlCharacterEscapes()); // XSS 이스케이프 클래스 주입
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}