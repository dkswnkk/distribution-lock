package com.example.coupontest.service;

import com.example.coupontest.annotation.DistributedLock;
import com.example.coupontest.enums.EventType;
import com.example.coupontest.service.issuer.GiftIssuer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GiftService {
    private static final String GIFT_SUFFIX = ":gift:";
    private static final Long TRUE = 1L;

    private final RedisTemplate<String, Object> redisTemplate;

    @DistributedLock(key = "#eventType.name()", waitTime = 5, leaseTime = 1)
    public void issueGiftToUser(String userId, EventType eventType, int count, GiftIssuer giftIssuer) {
        if (areAllGiftsClaimed(eventType, count)) {
            log.info("{} 이벤트의 기프티콘 재고가 남아있지 않습니다.", eventType);
            return;
        }

        issueGiftIfNotAlreadyReceived(userId, eventType, giftIssuer);
    }

    private boolean areAllGiftsClaimed(EventType eventType, int count) {
        return getCurrentGiftCount(eventType) >= count;
    }

    private void issueGiftIfNotAlreadyReceived(String userId, EventType eventType, GiftIssuer giftIssuer) {
        if (!TRUE.equals(registerUserToReceivedGifts(userId, eventType))) {
            log.info("{} 이용자는 {} 이벤트의 기프티콘을 이미 전송받았습니다.", userId, eventType);
            return;
        }

        log.info("{} 이벤트의 기프티콘이 {}에게 발행되었습니다.", eventType, userId);
        giftIssuer.issueGiftToUser(userId, eventType);
    }

    private Long registerUserToReceivedGifts(String userId, EventType eventType) {
        return redisTemplate.opsForSet().add(generateGiftSetKey(eventType), userId);
    }

    public Long getCurrentGiftCount(EventType eventType) {
        return redisTemplate.opsForSet().size(generateGiftSetKey(eventType));
    }

    public String generateGiftSetKey(EventType eventType) {
        return eventType.name().toLowerCase() + GIFT_SUFFIX;
    }
}