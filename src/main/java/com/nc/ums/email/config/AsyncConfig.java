package com.nc.ums.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

//Config 클래스에 추가
@Configuration
public class AsyncConfig {
	
	 @Bean("outboxTaskExecutor")
	 public ThreadPoolTaskExecutor outboxTaskExecutor() {
	     ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
	     exec.setCorePoolSize(10);   // 운영 환경에 맞게 조정
	     exec.setMaxPoolSize(50);
	     exec.setQueueCapacity(200);
	     exec.setThreadNamePrefix("outbox-worker-");
	     exec.initialize();
	     return exec;
	 }
}