-- 테스트용 더미 데이터 자동 삽입
INSERT INTO tb_payment_history (merchant_id, order_id, amount, approval_code, card_no, status, req_dt)
VALUES ('TEST_M001', 'ORD202603230001', 50000, '12345678', '1234-56**-****-1234', 'SUCCESS', CURRENT_TIMESTAMP);