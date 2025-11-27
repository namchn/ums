package com.nc.ums.service;

import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    //private final MessageRepository msgRepo;
    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelay = 5000)
    //@Transactional -> 원자화를 위해 로직을 나누어 처리함.
    public void dispatch() {
    	log.info("[OutboxDispatcher - dispatch]  ");
        
    	
        List<Outbox> rows = outboxRepo.findByPublishedFalse();
        for (Outbox r : rows) {
        	
        	// 실발송 로직을 만듬.
        	log.info("[OutboxDispatcher] publishing: " + r.getTopic() + " id=" + r.getAggregateId());
            //System.out.println("[OutboxDispatcher] publishing: " + r.getTopic() + " id=" + r.getAggregateId());
            
        	
        	// '선점(claim)' 용도로 쓰는 lock-free 방식의 CAS(Compare-And-Swap)
        	//CAS 방식으로 선점(optional: updateStatusIf) 하여 동시 처리 방지
        	//int updated  = msgRepo.updateStatusIf(r.getAggregateId()  , "NEW", "PROCESSING");
        	boolean claimed = outboxProcessor.processSingleMessage(r);
        	
        	// 인스턴스간 경쟁시   선점 성공 여부를 분기
        	if (!claimed) {
                // 선점 실패: 이미 PROCESSING/SUCCESS/FAILED 등 다른 상태
        		continue;
            }
        	
        	
        	
			/*
			 * 실발송 로직(정책)...
			 * SMTP
			 * 사내 메일서버 API
			 * 외부 발송 시스템(API Gateway)
			 * 예)emailSender.send(outbox.getPayload());
			 * 
			 * 여기서 지연 처리가 필요할 경우도 있음 (정책상의 속도 제한)
			 * 
			 */
        	
        	
        	//Outbox 패턴 or Transactional Outbox Pattern
        	//r.setPublished(true);// 실발송 시도 완료. - 큐 처리 여부
            //outboxRepo.save(r);
        }
    }
    
    
}
