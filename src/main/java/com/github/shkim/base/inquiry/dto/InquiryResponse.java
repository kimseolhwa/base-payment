package com.github.shkim.base.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "결제 조회 공통 응답 DTO")
public record InquiryResponse(
        @Schema(description = "결과 상태 코드", example = "0000")
        String resCd, 
        
        @Schema(description = "결과 상세 메시지", example = "정상 처리되었습니다.")
        String resMsg, 
        
        @Schema(description = "실제 비즈니스 응답 데이터")
        Object data
) {}