package com.nc.ums.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nc.ums.domain.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	
    Message findByMessageId(String messageId);
    
    @Modifying
    @Query("update Message m set m.status = :newStatus where m.messageId = :messageId and m.status = :oldStatus")
    int updateStatusIf(String messageId, String oldStatus, String newStatus);
}
