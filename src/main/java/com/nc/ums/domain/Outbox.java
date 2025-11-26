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
@Table(name = "outbox")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Outbox { 
	// 발송해야 할 이벤트 목록 저장(비동기 큐)
	// Outbox는 메시지를 실제로 “발송 엔진이 가져갈 수 있도록” 만들어 둔 큐 역할
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateId;
    private String topic;
    
    @Lob
    private String payload;
    
    private Boolean published = false;
    private LocalDateTime createdAt;

    @PrePersist
    public void pre() { createdAt = LocalDateTime.now(); }
}
