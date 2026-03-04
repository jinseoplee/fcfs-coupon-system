-- 쿠폰 테이블
CREATE TABLE coupon
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    total_quantity  INT          NOT NULL,
    issued_quantity INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
)ENGINE=InnoDB;

-- 쿠폰 발급 이력 테이블
CREATE TABLE coupon_issue
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id   BIGINT NOT NULL,
    issued_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_coupon_issue_coupon_id_user_id UNIQUE (coupon_id, user_id),
    CONSTRAINT fk_coupon_issue_coupon_coupon_id FOREIGN KEY (coupon_id) REFERENCES coupon (id),
    INDEX     idx_coupon_issue_user_id (user_id)
) ENGINE=InnoDB;