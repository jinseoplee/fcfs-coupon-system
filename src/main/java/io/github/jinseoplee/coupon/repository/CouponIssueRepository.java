package io.github.jinseoplee.coupon.repository;

import io.github.jinseoplee.coupon.domain.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByCouponIdAndUserId(Long couponId, Long userId);

    long countByCouponId(Long couponId);
}
