package com.example.coupontest.service.issuer;

import com.example.coupontest.enums.EventType;

public interface GiftIssuer {
    void issueGiftToUser(String userId, EventType eventType);
}
