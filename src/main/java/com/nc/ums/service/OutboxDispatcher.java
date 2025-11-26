package com.nc.ums.service;

import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nc.ums.domain.Outbox;
import com.nc.ums.repo.MessageRepository;
import com.nc.ums.repo.OutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling // 스케줄링 기능 활성화
@RequiredArgsConstructor
public class OutboxDispatcher {  // 발행(시뮬)
    private final OutboxRepository outboxRepo;
    private final MessageRepository msgRepo;

    @Scheduled(fixedDelay = 5000)
    public void dispatch() {
    	log.info("[OutboxDispatcher - dispatch]  ");
        
    	
        List<Outbox> rows = outboxRepo.findByPublishedFalse();
        for (Outbox r : rows) {
        	
        	// 실발송 로직을 만듬.
        	log.info("[OutboxDispatcher] publishing: " + r.getTopic() + " id=" + r.getAggregateId());
            //System.out.println("[OutboxDispatcher] publishing: " + r.getTopic() + " id=" + r.getAggregateId());
            
        	
        	//
        	//CAS 방식으로 선점(optional: updateStatusIf) 하여 동시 처리 방지
        	int claimed = msgRepo.updateStatusIf(r.getAggregateId()  , "NEW", "PROCESSING");
        	// 1 이면 처리 요청 ,0 은 이미 처리중임.
        	
			/*
			 * 실발송 로직(정책)...
			 * SMTP
			 * 사내 메일서버 API
			 * 외부 발송 시스템(API Gateway)
			 * 예)emailSender.send(outbox.getPayload());
			 */
        	
        	
        	
        	//Outbox 패턴 or Transactional Outbox Pattern
        	r.setPublished(true);// 실발송 시도 완료. - 큐 처리 여부
            outboxRepo.save(r);
        }
    }
}
