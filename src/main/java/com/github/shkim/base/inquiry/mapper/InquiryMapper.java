package com.github.shkim.base.inquiry.mapper;

import com.github.shkim.base.inquiry.dto.InquiryRequests;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 조회(Inquiry) 도메인 전용 MyBatis 매퍼 인터페이스.
 * <p>
 * PerformanceLoggingAspect에 의해 실행 시간 및 커넥션 풀 상태 자동 로깅 대상
 * </p>
 */
@Mapper
public interface InquiryMapper {

    /**
     * 가맹점 ID와 주문 ID 기반 결제 내역 단건 조회 수행.
     *
     * @param request 조회 조건을 담은 파라미터 레코드
     * @return 카멜케이스 키가 적용된 데이터 Map 반환
     */
    Map<String, Object> selectPaymentDetail(InquiryRequests.PaymentDetailReq request);
}