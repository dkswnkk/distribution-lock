package com.example.coupontest.service;

import com.example.coupontest.enums.EventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private GiftService giftService;

    @BeforeEach
    public void setup() {
        redisTemplate.delete(giftService.getGiftSetKey(EventType.MBTI));
    }

    @AfterEach
    public void tearDown() {
        redisTemplate.delete(giftService.getGiftSetKey(EventType.MBTI));
    }

    @Test
    @DisplayName("동시에 선물 발행시 최대 선물 수를 초과하지 않아야 함")
    void testConcurrentGiftIssuing_DoesNotExceedMaxGifts() throws InterruptedException {
        final int threadCount = 500;
        final int maxGifts = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    giftService.issueGiftForUser(UUID.randomUUID().toString(), EventType.MBTI, maxGifts);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long currentGiftCount = giftService.getGiftCount(EventType.MBTI);

        assertThat(currentGiftCount).isEqualTo(maxGifts);
    }

    @Test
    @DisplayName("중복 사용자 ID로 선물 발행시 중복 발행되지 않아야 함")
    void testGiftIssuing_NoDuplicateGiftsForSameUserId() throws InterruptedException {
        final String fixedUserId = "TEST_USER_ID";
        final int threadCount = 100;
        final int maxGifts = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    giftService.issueGiftForUser(fixedUserId, EventType.MBTI, maxGifts);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long currentGiftCount = giftService.getGiftCount(EventType.MBTI);

        assertThat(currentGiftCount).isEqualTo(1L);
    }
}
