package com.github.shkim.base.common.util;

import com.github.shkim.base.common.config.KmsKeyLoader;
import org.springframework.util.StringUtils;

/**
 * 애플리케이션 공통 암복호화 처리를 위한 유틸리티 클래스.
 * <p>
 * 기동 시 KMS로부터 캐싱된 도메인별(PG, 인증 등) 대칭키를 활용하여
 * 문자열 데이터 암복호화 수행
 * </p>
 */
public class CryptoUtils {

    /**
     * 주어진 평문을 지정된 도메인의 키로 암호화.
     *
     * @param domain 키를 조회할 도메인 명
     * @param plainText 암호화할 원본 평문 문자열
     * @return 암호화가 완료된 문자열 (데이터가 없을 경우 원본 반환)
     * @throws IllegalArgumentException 도메인에 해당하는 키셋이 없을 경우
     */
    public static String encrypt(String domain, String plainText) {
        if (!StringUtils.hasText(plainText)) return plainText;

        String secretKey = KmsKeyLoader.KEY_CACHE.get(domain);
        if (secretKey == null) {
            throw new IllegalArgumentException("KMS Key not found for domain: " + domain);
        }

        // 실제 프로젝트 연동 시 AES-256 등의 암호화 로직으로 대체
        return "[ENCRYPTED_WITH_" + secretKey + "]" + plainText;
    }

    /**
     * 주어진 암호문을 지정된 도메인의 키로 복호화.
     *
     * @param domain 키를 조회할 도메인 명
     * @param encryptedText 복호화할 암호문 문자열
     * @return 복호화가 완료된 평문 문자열
     */
    public static String decrypt(String domain, String encryptedText) {
        if (!StringUtils.hasText(encryptedText)) return encryptedText;

        String secretKey = KmsKeyLoader.KEY_CACHE.get(domain);
        if (secretKey == null) return encryptedText; // 키가 없으면 원본 리턴 (정책에 따라 예외 처리 가능)

        return encryptedText.replace("[ENCRYPTED_WITH_" + secretKey + "]", "");
    }
}