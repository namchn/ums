package com.nc.ums;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.nc.ums.repo.MessageRepository;
import com.nc.ums.repo.OutboxRepository;
import com.nc.ums.service.OutboxProcessor;

@SpringBootTest
class UmsApplicationTests {

	@Test
	void contextLoads() {
	}
	
	
	
	
	/*
	private final MessageRepository msgRepo;
    private final OutboxRepository outboxRepo;
    private final OutboxProcessor outboxProcessor;
	
	@Test
	void claim_success() {
	    when(outboxRepo.findById(anyLong())).thenReturn(Optional.of(out));
	    when(msgRepo.updateStatusIf(eq(msgId), eq("NEW"), eq("PROCESSING"))).thenReturn(1);
	    assertTrue(outboxProcessor.claimMessage(out));
	}
	*/


}
