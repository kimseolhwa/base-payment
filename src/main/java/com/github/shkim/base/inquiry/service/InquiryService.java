package com.github.shkim.base.inquiry.service;

import com.github.shkim.base.common.exception.DataNotFoundException;
import com.github.shkim.base.inquiry.dto.InquiryRequests;
import com.github.shkim.base.inquiry.dto.InquiryResponse;
import com.github.shkim.base.inquiry.mapper.InquiryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * DB 조회 비즈니스 로직을 전담하는 서비스 클래스.
 * <p>
 * MyBatis Mapper 연동 및 캐싱(Ehcache)을 통한 응답 속도 최적화 수행
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryMapper inquiryMapper;

    /**
     * 결제 상세 내역 단건 조회 및 로컬 캐싱 수행.
     * <p>
     * @Cacheable 적용으로 동일한 주문 ID 반복 조회 시 DB 부하 차단
     * </p>
     *
     * @param request 결제 조회 요청 파라미터 레코드
     * @return 조회 성공 시 결과 코드가 포함된 응답 레코드
     * @throws DataNotFoundException DB 조회 결과가 없을 경우
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "paymentCache", key = "#request.orderId()", unless = "#result == null")
    public InquiryResponse getPaymentDetail(InquiryRequests.PaymentDetailReq request) {
        log.info("[Inquiry Service] DB 실제 조회 실행 - orderId: {}", request.orderId());

        // 매퍼를 통한 DB 조회
        Map<String, Object> resultData = inquiryMapper.selectPaymentDetail(request);

        if (CollectionUtils.isEmpty(resultData)) {
            throw new DataNotFoundException("해당 주문 ID(" + request.orderId() + ")의 결제 내역이 존재하지 않습니다.");
        }

        // TODO: CryptoUtils.decrypt(...) 등을 활용하여 개인정보(카드번호 등) 복호화 로직 추가 지점

        return InquiryResponse.builder()
                .resCd("0000")
                .resMsg("정상 처리되었습니다.")
                .data(resultData)
                .build();
    }
}