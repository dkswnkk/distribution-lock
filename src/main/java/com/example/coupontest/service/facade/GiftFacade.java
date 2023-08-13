package com.example.coupontest.service.facade;

import com.example.coupontest.enums.EventType;
import com.example.coupontest.service.GiftService;
import com.example.coupontest.service.factory.GiftIssuerFactory;
import com.example.coupontest.service.issuer.GiftIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GiftFacade {
    private final GiftService giftService;
    private final GiftIssuerFactory giftIssuerFactory;

    public void issueGift(String userId, EventType eventType, int count) {
        GiftIssuer giftIssuer = giftIssuerFactory.getGiftIssuer(eventType);
        giftService.issueGiftToUser(userId, eventType, count, giftIssuer);
    }
}
