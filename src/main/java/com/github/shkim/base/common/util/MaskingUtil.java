package com.github.shkim.base.common.util;

import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 개인정보 및 민감 데이터 마스킹 처리를 위한 공통 유틸리티 클래스.
 * <p>
 * 정규식 룰(Rule)을 기반으로 문자열, Map, List 내의 민감 데이터(카드번호, 주민번호 등) 치환
 * </p>
 */
public class MaskingUtil {

    final static String DEFAULT_MASKING_CHAR = "*";

    /**
     * 마스킹 대상 및 치환 방식을 정의하는 룰 객체
     */
    @Getter
    public static class MaskingRule {
        private String key;
        private String regex;
        private Pattern pattern;
        private int start;
        private int end;
        private boolean fullMask;

        public MaskingRule(String key, String regex, int start, int end, boolean fullMask) {
            this.key = key;
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
            this.start = start;
            this.end = end;
            this.fullMask = fullMask;
        }
    }

    private static final List<MaskingRule> RULES = new ArrayList<>();
    static {
        RULES.add(new MaskingRule("cash_id_info", ".*", -1, -1, true));
        RULES.add(new MaskingRule("cash_tax_no", ".*", -1, -1, true));
        RULES.add(new MaskingRule("cardNumber", ".*", 4, 12, false));
    }

    /**
     * 구분자로 연결된 로그 문자열 내 민감 정보 마스킹 (기본 마스킹 문자 사용)
     *
     * @param logString 원본 로그 문자열
     * @param delimiter Key-Value 쌍을 구분하는 문자 (예: "&", ",")
     * @return 마스킹 처리된 로그 문자열
     */
    public static String logMasking(String logString, String delimiter) {
        return logMasking(logString, delimiter, DEFAULT_MASKING_CHAR);
    }

    /**
     * 구분자로 연결된 로그 문자열 내 민감 정보 마스킹
     *
     * @param logString 원본 로그 문자열
     * @param delimiter Key-Value 쌍을 구분하는 문자 (예: "&", ",")
     * @param maskChar 마스킹 문자
     * @return 마스킹 처리된 로그 문자열
     */
    public static String logMasking(String logString, String delimiter, String maskChar) {
        if (!StringUtils.hasText(logString)) return logString;
        if (!StringUtils.hasText(maskChar)) maskChar = DEFAULT_MASKING_CHAR;

        String[] entries = logString.split(Pattern.quote(delimiter));
        StringBuilder maskedLog = new StringBuilder();

        for (int i = 0; i < entries.length; i++) {
            String keyValue = entries[i];
            int eqIndex = keyValue.indexOf('=');

            if (eqIndex == -1) {
                maskedLog.append(keyValue);
            } else {
                String key = keyValue.substring(0, eqIndex).trim();
                String value = keyValue.substring(eqIndex + 1).trim();
                String maskedValue = maskValueByRules(key, value, maskChar);
                maskedLog.append(key).append("=").append(maskedValue);
            }

            if (i < entries.length - 1) {
                maskedLog.append(delimiter);
            }
        }
        return maskedLog.toString();
    }

    public static Map<String, Object> logMasking(Map<String, Object> map) {
        return logMasking(map, DEFAULT_MASKING_CHAR);
    }

    /**
     * JSON 형식 등으로 파싱된 Map 자료구조 내부의 민감 정보 재귀적 마스킹
     *
     * @param map 원본 Map 객체
     * @param maskChar 치환할 문자
     * @return 마스킹이 완료된 새로운 Map 객체
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> logMasking(Map<String, Object> map, String maskChar) {
        if (CollectionUtils.isEmpty(map)) return map;
        if (!StringUtils.hasText(maskChar)) maskChar = DEFAULT_MASKING_CHAR;

        Map<String, Object> maskedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                maskedMap.put(key, maskValueByRules(key, (String) value, maskChar));
            } else if (value instanceof Map) {
                maskedMap.put(key, logMasking((Map<String, Object>) value, maskChar));
            } else if (value instanceof List) {
                List<Object> maskedList = new ArrayList<>();
                for (Object item : (List<Object>) value) {
                    if (item instanceof Map) {
                        maskedList.add(logMasking((Map<String, Object>) item, maskChar));
                    } else {
                        maskedList.add(item);
                    }
                }
                maskedMap.put(key, maskedList);
            } else {
                maskedMap.put(key, value);
            }
        }
        return maskedMap;
    }

    private static String maskValueByRules(String key, String value, String maskChar) {
        for (MaskingRule rule : RULES) {
            if (key.equalsIgnoreCase(rule.getKey()) && rule.getPattern().matcher(value).matches()) {
                return applyMask(value, rule, maskChar);
            }
        }
        return value;
    }

    private static String applyMask(String value, MaskingRule rule, String maskChar) {
        if (rule.isFullMask()) {
            return maskChar.repeat(value.length());
        }

        int start = Math.max(0, rule.getStart());
        int end = Math.min(value.length(), rule.getEnd());
        if (start >= end) return value;

        StringBuilder sb = new StringBuilder();
        sb.append(value.substring(0, start));
        sb.append(maskChar.repeat(end - start));
        sb.append(value.substring(end));
        return sb.toString();
    }
}