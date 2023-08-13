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
        if (hasReachedMaxGiftCount(eventType, count)) {
            log.info("Sorry, all gifts for {} have been claimed.", eventType);
            return;
        }
        Long added = redisTemplate.opsForSet().add(getGiftKeyPrefix(eventType), userId);
        if (Boolean.TRUE.equals(added)) {
            log.info("Gift for {} issued to user {}.", eventType, userId);
        } else {
            log.info("User {} already has a gift for {}.", userId, eventType);
        }

    }

    private boolean hasReachedMaxGiftCount(EventType eventType, int count) {
        Long currentGiftCount = getGiftCount(eventType);
        return currentGiftCount >= count;
    }

    public Long getGiftCount(EventType eventType) {
        return redisTemplate.opsForSet().size(getGiftKeyPrefix(eventType));
    }

    public String getGiftKeyPrefix(EventType eventType) {
        return eventType.name().toLowerCase() + ":gift:";
    }
}