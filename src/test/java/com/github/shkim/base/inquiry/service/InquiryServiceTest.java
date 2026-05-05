package com.github.shkim.base.inquiry.service;

import com.github.shkim.base.common.exception.ResponseCode;
import com.github.shkim.base.common.util.MessageUtils;
import com.github.shkim.base.inquiry.dto.InquiryRequests;
import com.github.shkim.base.inquiry.dto.InquiryResponse;
import com.github.shkim.base.inquiry.mapper.InquiryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryMapper inquiryMapper;

    @Mock
    private MessageUtils messageUtils; // MessageUtils Mock 추가

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    @DisplayName("결제 내역 조회 성공 테스트 - DB에서 데이터를 정상적으로 가져와 응답 객체로 변환한다.")
    void getPaymentDetail_Success() {
        // given
        InquiryRequests.PaymentDetailReq req = new InquiryRequests.PaymentDetailReq("TEST_M001", "ORD001");

        Map<String, Object> mockDbResult = new HashMap<>();
        mockDbResult.put("AMOUNT", 50000);
        mockDbResult.put("STATUS", "SUCCESS");
        // 복호화 테스트를 위해 암호화된 더미 카드번호 추가
        mockDbResult.put("card_no", "[ENCRYPTED_WITH_null]12345678");

        // 매퍼 및 MessageUtils Mocking
        given(inquiryMapper.selectPaymentDetail(any(InquiryRequests.PaymentDetailReq.class))).willReturn(mockDbResult);
        given(messageUtils.getMessage(eq(ResponseCode.SUCCESS.getMessageKey()))).willReturn("Processed successfully.");

        // when
        InquiryResponse response = inquiryService.getPaymentDetail(req);

        // then
        assertThat(response.resCd()).isEqualTo(ResponseCode.SUCCESS.getCode());
        assertThat(response.resMsg()).isEqualTo("Processed successfully.");

        // 응답 객체의 data 필드 검증
        @SuppressWarnings("unchecked")
        Map<String, Object> responseData = (Map<String, Object>) response.data();
        assertThat(responseData.get("STATUS")).isEqualTo("SUCCESS");
        // 복호화 결과(암호화 접두어가 제거되었는지) 확인
        assertThat(responseData.get("card_no")).isEqualTo("12345678");

        verify(inquiryMapper).selectPaymentDetail(req);
    }
}