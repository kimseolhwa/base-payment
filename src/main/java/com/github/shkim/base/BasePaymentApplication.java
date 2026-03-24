package com.github.shkim.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

/**
 * base-payment 프로젝트의 최상단 메인 구동 클래스.
 * <p>
 * Spring Boot 애플리케이션 기동, 하위 패키지 컴포넌트 스캔,
 * 로컬 캐시 활성화 및 커스텀 프로퍼티 스캔 수행
 * </p>
 */
@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
public class BasePaymentApplication {

    /**
     * 내장 톰캣 서버를 구동하고 스프링 컨텍스트 초기화 수행
     *
     * @param args 실행 시 전달받는 커맨드라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(BasePaymentApplication.class, args);
    }
}