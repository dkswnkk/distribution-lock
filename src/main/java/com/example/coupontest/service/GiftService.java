package com.example.coupontest.service;

import com.example.coupontest.annotation.DistributedLock;
import com.example.coupontest.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftService {
    private final RedisTemplate<String, Object> redisTemplate;

    @DistributedLock(key = "#eventType.name()", waitTime = 5, leaseTime = 1)
    public void issueGiftForUser(String userId, EventType eventType, int count) {
        Long currentGiftCount = getGiftCount(eventType);

        if (currentGiftCount >= count) {
            log.info("Sorry, all gifts for {} have been claimed.", eventType);
            return;
        }

        Long added = redisTemplate.opsForSet().add(getGiftSetKey(eventType), userId);
        if (Boolean.TRUE.equals(added)) {
            log.info("Gift for {} issued to user {}.", eventType, userId);
            issueGift(userId, eventType);
        } else {
            log.info("User {} already has a gift for {}.", userId, eventType);
        }
    }
    private void issueGift(String userId, EventType eventType) {
        // TODO: 기프티콘 전송 로직
        log.debug("Issuing the actual gift for user {} for event type {}.", userId, eventType);
    }

    public Long getGiftCount(EventType eventType) {
        return redisTemplate.opsForSet().size(getGiftSetKey(eventType));
    }

    public String getGiftSetKey(EventType eventType) {
        return eventType.name().toLowerCase() + ":gift:";
    }
}
