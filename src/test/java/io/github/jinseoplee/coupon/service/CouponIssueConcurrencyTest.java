package io.github.jinseoplee.coupon.service;

import io.github.jinseoplee.config.TestcontainersConfiguration;
import io.github.jinseoplee.coupon.domain.Coupon;
import io.github.jinseoplee.coupon.exception.CouponOutOfStockException;
import io.github.jinseoplee.coupon.repository.CouponIssueRepository;
import io.github.jinseoplee.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CouponIssueConcurrencyTest {

    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @BeforeEach
    void setUp() {
        couponIssueRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 발급 수량이 재고를 초과해서는 안 된다")
    void issue_ShouldNotExceedTotalQuantity_UnderConcurrency() throws InterruptedException {
        // given
        final int totalRequests = 1000;
        final int totalQuantity = 100;
        long couponId = createCoupon(totalQuantity);

        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();
        AtomicInteger unexpectedCount = new AtomicInteger();

        // when
        for (long userId = 1; userId <= totalRequests; userId++) {
            final long uid = userId;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    couponIssueService.issue(couponId, uid);
                    successCount.incrementAndGet();
                } catch (CouponOutOfStockException e) {
                    rejectedCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();

        shutdownExecutor(executor);

        // then
        assertIssuanceResult(
                couponId,
                totalRequests,
                totalQuantity,
                successCount.get(),
                rejectedCount.get(),
                unexpectedCount.get()
        );
    }

    private long createCoupon(int totalQuantity) {
        Coupon coupon = Coupon.builder()
                .title("테스트 쿠폰")
                .totalQuantity(totalQuantity)
                .build();
        return couponRepository.save(coupon).getId();
    }

    private void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }

    private void assertIssuanceResult(long couponId, int totalRequests, int totalQuantity,
                                      int success, int rejected, int unexpected) {
        Coupon savedCoupon = couponRepository.findById(couponId).orElseThrow();
        long issuedCount = couponIssueRepository.countByCouponId(couponId);

        assertSoftly(softly -> {
            softly.assertThat(savedCoupon.getIssuedQuantity())
                    .as("쿠폰 엔티티 발급 수량").isEqualTo(totalQuantity);

            softly.assertThat(issuedCount)
                    .as("쿠폰 발급 이력 수량").isEqualTo(totalQuantity);

            softly.assertThat(success)
                    .as("쿠폰 발급 성공 횟수").isEqualTo(totalQuantity);

            softly.assertThat(rejected)
                    .as("쿠폰 발급 거절 횟수").isEqualTo(totalRequests - totalQuantity);

            softly.assertThat(unexpected)
                    .as("예상치 못한 예외 발생 건수").isZero();
        });
    }
}