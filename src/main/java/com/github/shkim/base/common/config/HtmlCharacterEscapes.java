package com.github.shkim.base.common.config;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import org.apache.commons.text.StringEscapeUtils;

/**
 * JSON 직렬화 시 XSS(크로스 사이트 스크립팅) 방지를 위한 특수문자 이스케이프 처리기.
 * <p>
 * Jackson ObjectMapper에 등록되어 API 응답 데이터(JSON) 내의 악성 스크립트 실행 원천 차단
 * </p>
 */
public class HtmlCharacterEscapes extends CharacterEscapes {

    private final int[] asciiEscapes;

    /**
     * 이스케이프 대상 아스키코드 배열 초기화 수행.
     * <p>
     * 기본 아스키코드 배열을 가져온 후, XSS 공격에 자주 사용되는 특수문자(<, >, ", ', &)를
     * 커스텀 이스케이프 대상으로 강제 지정
     * </p>
     */
    public HtmlCharacterEscapes() {
        asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
        asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['\"'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['&'] = CharacterEscapes.ESCAPE_CUSTOM;
    }

    /**
     * 커스텀 설정이 적용된 아스키코드 이스케이프 매핑 배열 반환.
     *
     * @return 이스케이프 룰이 적용된 int 배열
     */
    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }

    /**
     * 커스텀 대상으로 지정된 문자를 안전한 HTML4 표준 엔티티로 변환하여 반환.
     *
     * @param ch 변환할 대상 문자의 아스키코드 정수값
     * @return HTML 엔티티로 치환된 안전한 문자구열 (예: < -> &lt;)
     */
    @Override
    public SerializableString getEscapeSequence(int ch) {
        char charAt = (char) ch;
        // commons-text 라이브러리를 활용하여 HTML4 규격으로 안전하게 이스케이프 처리
        return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString(charAt)));
    }
}