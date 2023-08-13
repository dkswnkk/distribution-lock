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
        if (canIssueGift(userId, eventType, count)) {
            issueGift(userId, eventType);
            incrementGiftCount(eventType);
            // kafka event 발행(기프티콘 전송)
            // kafka event 발행(발행 로그 저장)
        }
    }

    private boolean canIssueGift(String userId, EventType eventType, int count) {
        return !hasReachedMaxGiftCount(eventType, count) && !hasGiftForUser(userId, eventType);
    }


    private boolean hasReachedMaxGiftCount(EventType eventType, int count) {
        Long currentGiftCount = getGiftCount(eventType);
        if (currentGiftCount >= count) {
            log.info("Sorry, all gifts for {} have been claimed.", eventType);
            return true;
        }
        return false;
    }

    private boolean hasGiftForUser(String userId, EventType eventType) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(getGiftKeyPrefix(eventType) + userId))) {
            log.info("User {} already has a gift for {}.", userId, eventType);
            return true;
        }
        return false;
    }

    private void issueGift(String userId, EventType eventType) {
        redisTemplate.opsForValue().set(getGiftKeyPrefix(eventType) + userId, "Gift for " + userId);
        log.info("Gift for {} issued to user {}.", eventType, userId);
    }

    private void incrementGiftCount(EventType eventType) {
        redisTemplate.opsForValue().increment(getGiftCountKey(eventType));
    }

    public Long getGiftCount(EventType eventType) {
        Object rawValue = redisTemplate.opsForValue().get(getGiftCountKey(eventType));
        return rawValue == null ? 0L : Long.parseLong(rawValue.toString());
    }

    public String getGiftKeyPrefix(EventType eventType) {
        return eventType.name().toLowerCase() + ":gift:";
    }

    public String getGiftCountKey(EventType eventType) {
        return eventType.name().toLowerCase() + ":giftCount";
    }
}