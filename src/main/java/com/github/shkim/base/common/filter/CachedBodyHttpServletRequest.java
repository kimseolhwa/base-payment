package com.github.shkim.base.common.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 요청의 본문(Body) 데이터를 메모리에 캐싱하여 여러 번 읽을 수 있도록 지원하는 커스텀 래퍼.
 * <p>
 * 서블릿의 기본 InputStream은 한 번 읽으면 소멸되므로, 필터(로깅, 식별자 추출 등)에서
 * 본문을 읽은 후에도 컨트롤러가 다시 데이터를 읽을 수 있도록 스트림 복제 수행
 * </p>
 */
@Getter
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    /**
     * 원본 요청의 스트림 데이터를 읽어내어 내부 byte 배열로 복사 및 저장
     *
     * @param request 클라이언트의 원본 HTTP 요청 객체
     * @throws IOException 스트림 읽기 중 입출력 오류 발생 시
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 생성되는 순간 InputStream을 읽어서 byte 배열로 저장 (이후 무한히 읽기 가능)
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * 캐싱된 byte 배열을 기반으로 새로운 ServletInputStream 객체 생성 및 반환.
     * <p>
     * 컨트롤러의 @RequestBody 등이 호출할 때마다 매번 새로운 스트림 제공
     * </p>
     *
     * @return 캐시 데이터가 담긴 커스텀 ServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    /**
     * 캐싱된 byte 배열을 기반으로 새로운 BufferedReader 객체 생성 및 반환
     *
     * @return 캐시 데이터가 담긴 BufferedReader
     */
    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }

    /**
     * 메모리에 캐싱된 원본 데이터의 byte 배열 직접 반환
     *
     * @return 요청 본문의 byte 배열
     */
    public byte[] getCachedBody() {
        return this.cachedBody;
    }

    /**
     * 캐싱된 메모리 데이터(byte 배열)를 읽어들이기 위한 커스텀 ServletInputStream 내부 구현체
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        /**
         * 주어진 byte 배열을 소스로 하는 입력 스트림 버퍼 초기화
         *
         * @param contents 캐싱된 요청 본문 byte 배열
         */
        public CachedBodyServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }
        @Override
        public int read() { return buffer.read(); }
        @Override
        public boolean isFinished() { return buffer.available() == 0; }
        @Override
        public boolean isReady() { return true; }
        @Override
        public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException(); }
    }
}