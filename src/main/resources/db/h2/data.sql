-- 테스트용 가맹점 더미 데이터 삽입 (API 인증용)
INSERT INTO merchant_info (merchant_id, secret_key, merchant_name, status, reg_dt)
VALUES ('TEST_M001', 'sec_test_abc123', '테스트 가맹점', 'ACTIVE', CURRENT_TIMESTAMP);

-- 테스트용 더미 데이터 자동 삽입
INSERT INTO payment_history (merchant_id, order_id, amount, approval_code, card_no, status, req_dt)
VALUES ('TEST_M001', 'ORD202603230001', 50000, '12345678', '1234-56**-****-1234', 'SUCCESS', CURRENT_TIMESTAMP);