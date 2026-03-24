package com.github.shkim.base.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * application.yml의 app.kms 하위 속성들을 매핑하는 불변 프로퍼티 레코드.
 * <p>
 * Java 16+의 record를 활용하여 Setter 없이 생성자 바인딩 수행
 * </p>
 * @param keySets 도메인별 키셋 ID 맵
 * @param serverUrl KMS 서버 연동 URL
 */
@ConfigurationProperties(prefix = "app.kms")
public record KmsProperties(Map<String, String> keySets, String serverUrl) {
}