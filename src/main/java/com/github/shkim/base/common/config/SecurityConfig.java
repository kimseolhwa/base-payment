package com.github.shkim.base.common.config;

import com.github.shkim.base.common.security.MerchantAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MerchantAuthenticationFilter merchantAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 방어 설정
            .csrf(csrf -> csrf
                // API 엔드포인트는 CSRF 방어에서 예외 처리
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            // 2. 세션 고정 보호 
            .sessionManagement(session -> session
                .sessionFixation().changeSessionId()
            )
            // 3. 헤더 보안 설정 보강
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // H2 콘솔 접근 등을 위해 X-Frame-Options: SAMEORIGIN
            )
            // 4. 경로별 접근 제어
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 필터(MerchantAuthenticationFilter)에서 직접 검증하므로 Security 단에서는 우선 전부 허용 처리
            )
            // 5. 커스텀 인증 필터 등록
            // UsernamePasswordAuthenticationFilter(기본 폼 로그인 필터)가 동작하기 전에 우리 커스텀 필터가 먼저 검증하도록 배치
            .addFilterBefore(merchantAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}