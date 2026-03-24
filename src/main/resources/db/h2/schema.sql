-- 애플리케이션 기동 시 테이블 자동 생성
CREATE TABLE IF NOT EXISTS tb_payment_history (
    merchant_id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    approval_code VARCHAR(20),
    card_no VARCHAR(50),
    status VARCHAR(10),
    req_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (merchant_id, order_id)
 );

-- 노티 발송 이력 테이블
CREATE TABLE IF NOT EXISTS tb_noti_history (
   seq BIGINT AUTO_INCREMENT PRIMARY KEY,
   merchant_id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(10),
    send_dt TIMESTAMP
 );