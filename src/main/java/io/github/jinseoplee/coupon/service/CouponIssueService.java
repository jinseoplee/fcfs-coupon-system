package io.github.jinseoplee.coupon.service;

import io.github.jinseoplee.coupon.domain.Coupon;
import io.github.jinseoplee.coupon.domain.CouponIssue;
import io.github.jinseoplee.coupon.exception.CouponAlreadyIssuedException;
import io.github.jinseoplee.coupon.exception.CouponNotFoundException;
import io.github.jinseoplee.coupon.repository.CouponIssueRepository;
import io.github.jinseoplee.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public void issue(Long couponId, Long userId) {
        // 쿠폰 조회
        Coupon coupon = findCouponWithLock(couponId);

        // 중복 발급 확인
        checkAlreadyIssued(couponId, userId);

        // 쿠폰 발급
        coupon.issue();

        // 쿠폰 발급 이력 저장
        saveCouponIssue(coupon, userId);
    }

    private Coupon findCouponWithLock(Long couponId) {
        return couponRepository.findByIdWithLock(couponId)
                .orElseThrow(CouponNotFoundException::new);
    }

    private void checkAlreadyIssued(Long couponId, Long userId) {
        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw new CouponAlreadyIssuedException();
        }
    }

    private void saveCouponIssue(Coupon coupon, Long userId) {
        CouponIssue couponIssue = CouponIssue.builder()
                .coupon(coupon)
                .userId(userId)
                .build();

        couponIssueRepository.save(couponIssue);
    }
}
