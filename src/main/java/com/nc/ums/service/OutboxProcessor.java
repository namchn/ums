package com.nc.ums.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nc.ums.domain.Outbox;
import com.nc.ums.repo.MessageRepository;
import com.nc.ums.repo.OutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final MessageRepository msgRepo;
    private final OutboxRepository outboxRepo;
    
    // 하나의 메시지를 처리하는 단위 트랜잭션 메소드
    @Transactional // 여기서 트랜잭션 시작
    public boolean processSingleMessage(Outbox r) {
        
    	// 1. 선점/업데이트 (DB 작업 - 매우 빠르다고 하나 측정필요)
        // 1. CAS 방식으로 선점 시도
        int updated = msgRepo.updateStatusIf(r.getAggregateId(), "NEW", "PROCESSING");

        // 1 이면 처리 요청 ,0 은 이미 처리중임.
        if (updated == 0) {
            log.info("Message id={} already processed. Skipping.", r.getAggregateId());
            return false; // 선점 실패, 트랜잭션 커밋 후 종료
        }

        log.info("Message id={} successfully claimed. Proceeding with dispatch.", r.getAggregateId());

        // 2. (선택사항) 트랜잭션 커밋 후 외부 시스템 호출
        // 외부 호출은 트랜잭션과 독립적으로 처리하는 것이 좋습니다.
        // 이 부분은  Dispatcher 로직에서 처리하거나,
        // TransactionSynchronizationManager를 사용하여 커밋 성공 시 실행되도록 할 수 있습니다.

        // 3. DB 상태 업데이트 (DB 작업 - 매우 빠르다고 하나 측정필요)
        //Outbox 패턴 or Transactional Outbox Pattern
        r.setPublished(true);
        outboxRepo.save(r);// 실발송 시도 완료. - 큐 처리 여부
        
        //--> 트랜잭션은 여기서 즉시 커밋되고 끝납니다. DB 커넥션 반환
        return true;
    }

}
