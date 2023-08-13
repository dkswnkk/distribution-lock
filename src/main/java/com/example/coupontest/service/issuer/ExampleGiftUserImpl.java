package com.example.coupontest.service.issuer;

import com.example.coupontest.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExampleGiftUserImpl implements GiftIssuer {
    @Override
    public void issueGiftToUser(String userId, EventType eventType) {
        // TODO: 기프티콘 전송 로직 (외부 API 호출)
        // EVENT 발행 필요
        log.info("{}에게 {}이벤트의 기프티콘이 실제로 발행되었습니다.", userId, eventType);
    }
}
