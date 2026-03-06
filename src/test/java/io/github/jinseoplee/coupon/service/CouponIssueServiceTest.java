package io.github.jinseoplee.coupon.service;

import io.github.jinseoplee.coupon.domain.Coupon;
import io.github.jinseoplee.coupon.domain.CouponIssue;
import io.github.jinseoplee.coupon.exception.CouponAlreadyIssuedException;
import io.github.jinseoplee.coupon.exception.CouponNotFoundException;
import io.github.jinseoplee.coupon.exception.CouponOutOfStockException;
import io.github.jinseoplee.coupon.repository.CouponIssueRepository;
import io.github.jinseoplee.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @InjectMocks
    private CouponIssueService couponIssueService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    private Long couponId;
    private Long userId;

    @BeforeEach
    void setUp() {
        couponId = 1L;
        userId = 12L;
    }

    @Test
    @DisplayName("요청이 유효하면 쿠폰 발급에 성공한다")
    void issue_ShouldSucceed_WhenRequestIsValid() {
        // given
        Coupon coupon = createCoupon(10);

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)).willReturn(false);

        // before 상태 검증
        assertThat(coupon.getIssuedQuantity()).isZero();

        // when
        couponIssueService.issue(couponId, userId);

        // then
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        verify(couponIssueRepository).save(any(CouponIssue.class));
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않으면 CouponNotFoundException 예외가 발생한다")
    void issue_ShouldThrowCouponNotFoundException_WhenCouponNotFound() {
        // given
        given(couponRepository.findById(couponId)).willReturn(Optional.empty());

        // when & then
        assertThrows(CouponNotFoundException.class,
                () -> couponIssueService.issue(couponId, userId));

        verify(couponIssueRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 발급된 쿠폰이면 CouponAlreadyIssuedException 예외가 발생한다")
    void issue_ShouldThrowCouponAlreadyIssuedException_WhenAlreadyIssued() {
        // given
        Coupon coupon = createCoupon(10);

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)).willReturn(true);

        // when & then
        assertThrows(CouponAlreadyIssuedException.class,
                () -> couponIssueService.issue(couponId, userId));

        verify(couponIssueRepository, never()).save(any());
    }

    @Test
    @DisplayName("재고가 부족하면 CouponOutOfStockException 예외가 발생한다")
    void issue_ShouldThrowCouponOutOfStockException_WhenOutOfStock() {
        // given
        Coupon coupon = createCoupon(1);
        coupon.issue(); // 재고 소진 상태

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)).willReturn(false);

        // when & then
        assertThrows(CouponOutOfStockException.class,
                () -> couponIssueService.issue(couponId, userId));

        verify(couponIssueRepository, never()).save(any());
    }

    private Coupon createCoupon(int totalQuantity) {
        return Coupon.builder()
                .title("테스트 쿠폰")
                .totalQuantity(totalQuantity)
                .build();
    }
}