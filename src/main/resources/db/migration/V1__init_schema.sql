-- 쿠폰 캠페인 테이블
CREATE TABLE coupon_campaign
(
    id             BIGINT NOT NULL AUTO_INCREMENT,
    total_quantity INT    NOT NULL,
    issued_count   INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CHECK (issued_count >= 0 AND issued_count <= total_quantity)
);

-- 쿠폰 발급 테이블
CREATE TABLE coupon_issue
(
    id          BIGINT    NOT NULL AUTO_INCREMENT,
    campaign_id BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    issued_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_campaign_user (campaign_id, user_id),
    CONSTRAINT fk_campaign FOREIGN KEY (campaign_id)
        REFERENCES coupon_campaign (id)
        ON DELETE CASCADE
);

-- 조회 최적화 인덱스
CREATE INDEX idx_coupon_issue_campaign_id ON coupon_issue (campaign_id);