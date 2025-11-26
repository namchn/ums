package com.nc.ums.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "send_failure")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class SendFailure {
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String messageId;
    
    @Lob
    private String payload;
    private String errorType;
    
    @Lob
    private String errorMessage;
    private LocalDateTime occurredAt;
    private Boolean handled = false;
    private String handledBy;
    private LocalDateTime handledAt;

    @PrePersist
    public void pre() { occurredAt = LocalDateTime.now(); }
}
