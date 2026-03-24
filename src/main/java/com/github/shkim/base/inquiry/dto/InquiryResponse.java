package com.github.shkim.base.inquiry.dto;

import lombok.Builder;

/**
 * 조회(Inquiry) 도메인 공통 API 응답 DTO.
 * <p>
 * 비즈니스 로직 처리 후 결과 코드, 메시지, 상세 데이터를 감싸서 반환 처리
 * </p>
 *
 * @param resCd 결과 상태 코드 (정상: 0000)
 * @param resMsg 결과 상세 메시지
 * @param data 실제 비즈니스 응답 데이터 객체 (또는 Map)
 */
@Builder
public record InquiryResponse(String resCd, String resMsg, Object data) {}