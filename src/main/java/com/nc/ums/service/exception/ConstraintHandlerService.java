package com.nc.ums.service.exception;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nc.ums.domain.Message;
import com.nc.ums.domain.Outbox;
import com.nc.ums.repo.MessageRepository;
import com.nc.ums.repo.OutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConstraintHandlerService {
    private final MessageRepository msgRepo;
    private final OutboxRepository outboxRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean handleConstraintInNewTx(String messageId) {
        Message existing = msgRepo.findByMessageId(messageId);
        if (existing == null) {
            throw new RuntimeException("Insert failed but no existing record for messageId=" + messageId);
        }
        
        if ("NEW".equals(existing.getStatus())) {
            return false;
        }
        if ("SUCCESS".equals(existing.getStatus())) {
            return false;
        }
        if ("PROCESSING".equals(existing.getStatus())) {
            return false;
        }

        //  정책에 따라 상태값에 대하여 어떻게 처리할지 달라짐.
        if ("FAILED".equals(existing.getStatus())) {
            Outbox out = Outbox.builder()
                    .aggregateId(messageId)
                    .topic("message-topic")
                    .payload(existing.getPayload())
                    .published(false)
                    .build();
            outboxRepo.save(out);
            return true;
        }
        
        return true;
    }
}
