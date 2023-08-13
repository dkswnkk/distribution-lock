package com.example.coupontest.service.factory;

import com.example.coupontest.enums.EventType;
import com.example.coupontest.service.issuer.ExampleGiftUserImpl;
import com.example.coupontest.service.issuer.GiftIssuer;
import org.springframework.stereotype.Service;

@Service
public class GiftIssuerFactory {
    public GiftIssuer getGiftIssuer(EventType eventType) {
        if (eventType == EventType.MBTI) {
            return new ExampleGiftUserImpl();
        }
        throw new IllegalArgumentException("Unknown issuer type");
    }
}

