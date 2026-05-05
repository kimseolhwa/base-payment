package com.github.shkim.base.inquiry.service;

import com.github.shkim.base.common.exception.DataNotFoundException;
import com.github.shkim.base.common.exception.ResponseCode;
import com.github.shkim.base.common.util.CryptoUtils;
import com.github.shkim.base.common.util.MessageUtils;
import com.github.shkim.base.inquiry.dto.InquiryRequests;
import com.github.shkim.base.inquiry.dto.InquiryResponse;
import com.github.shkim.base.inquiry.mapper.InquiryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * DB 조회 비즈니스 로직을 전담하는 서비스 클래스.
 * <p>
 * MyBatis Mapper 연동을 통한 데이터 단건 조회 수행
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryMapper inquiryMapper;
    private final MessageUtils messageUtils;

    /**
     * 결제 상세 내역 단건 조회 수행.
     * <p>
     * (캐시 무효화 정책이 없어 @Cacheable 제거, 항상 DB 직접 조회하도록 변경)
     * </p>
     *
     * @param request 결제 조회 요청 파라미터 레코드
     * @return 조회 성공 시 결과 코드가 포함된 응답 레코드
     * @throws DataNotFoundException DB 조회 결과가 없을 경우
     */
    @Transactional(readOnly = true)
    public InquiryResponse getPaymentDetail(InquiryRequests.PaymentDetailReq request) {
        log.info("[Inquiry Service] DB 실제 조회 실행 - orderId: {}", request.orderId());

        // 매퍼를 통한 DB 조회
        Map<String, Object> resultData = inquiryMapper.selectPaymentDetail(request);

        if (CollectionUtils.isEmpty(resultData)) {
            throw new DataNotFoundException("해당 주문 ID(" + request.orderId() + ")의 결제 내역이 존재하지 않습니다.");
        }

        // CryptoUtils를 활용하여 카드번호(card_no) 등 민감 정보 복호화 로직 적용
        if (resultData.containsKey("card_no") && resultData.get("card_no") != null) {
            String encryptedCardNo = String.valueOf(resultData.get("card_no"));
            // 도메인 키 "payment"는 예시이며, 실제 KMS 키에 맞게 조정 필요
            String decryptedCardNo = CryptoUtils.decrypt("payment", encryptedCardNo);
            resultData.put("card_no", decryptedCardNo);
        }

        return InquiryResponse.builder()
                .resCd(ResponseCode.SUCCESS.getCode())
                .resMsg(messageUtils.getMessage(ResponseCode.SUCCESS.getMessageKey()))
                .data(resultData)
                .build();
    }
}