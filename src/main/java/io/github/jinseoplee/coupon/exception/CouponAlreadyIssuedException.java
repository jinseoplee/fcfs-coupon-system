package io.github.jinseoplee.coupon.exception;

public class CouponAlreadyIssuedException extends RuntimeException {

    public CouponAlreadyIssuedException() {
        super("이미 발급된 쿠폰입니다.");
    }
}
