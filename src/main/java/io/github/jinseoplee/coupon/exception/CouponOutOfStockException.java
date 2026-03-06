package io.github.jinseoplee.coupon.exception;

public class CouponOutOfStockException extends RuntimeException {

    public CouponOutOfStockException() {
        super("쿠폰 수량이 모두 소진되었습니다.");
    }
}
