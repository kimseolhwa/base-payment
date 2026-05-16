package com.github.shkim.base.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 방어 설정 (보안 강화)
            // 브라우저 환경(UI 연동 도메인)에서는 CSRF 방어가 필수입니다.
            // 하지만 현재 구조상 /api/** 는 REST API이므로 CSRF 검증에서 제외할 수 있습니다.
            // 필요에 따라 전체를 enable 하고 CookieCsrfTokenRepository 등을 활용할 수 있습니다.
            .csrf(csrf -> csrf
                // API 엔드포인트는 CSRF 방어에서 예외 처리 (주로 S2S 통신이거나 모바일 앱일 경우)
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            // 2. 세션 고정 보호 (Session Fixation Protection)
            // 사용자가 인증될 때마다 새로운 세션 ID를 발급하여 세션 하이재킹을 방어합니다.
            .sessionManagement(session -> session
                .sessionFixation().changeSessionId()
                // 필요시 세션 생성 정책을 제어 (UI 도메인은 ALWAYS/IF_REQUIRED, API는 STATELESS 등 복합 적용 가능)
                // 현재는 혼합된 상태이므로 기본값 유지
            )
            // 3. 헤더 보안 설정 보강 (기존 SecurityHeaderFilter 역할을 스프링 시큐리티에 일부 위임)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // H2 콘솔 접근 등을 위해 X-Frame-Options: SAMEORIGIN
            )
            // 4. 경로별 접근 제어 (기본적으로 모두 허용 상태에서 시작하여 점진적으로 닫아나가는 것이 좋습니다)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 현재는 별도의 인증(로그인) 기능이 없으므로 전부 허용
            );

        return http.build();
    }
}