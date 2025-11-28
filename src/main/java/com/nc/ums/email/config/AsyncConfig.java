package com.nc.ums.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

//Config 클래스에 추가
@Configuration
@EnableScheduling  // 스케줄링 기능 활성화
public class AsyncConfig {
	
	 @Bean("outboxTaskExecutor")
	 public ThreadPoolTaskExecutor outboxTaskExecutor(
	            @Value("${outbox.executor.core:10}") int core,
	            @Value("${outbox.executor.max:50}") int max,
	            @Value("${outbox.executor.queue:200}") int queue) {
	     ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
	     exec.setCorePoolSize(core);   // 운영 환경에 맞게 조정
	     exec.setMaxPoolSize(max);
	     exec.setQueueCapacity(queue);
	     exec.setThreadNamePrefix("outbox-worker-");
	     exec.initialize();
	     return exec;
	 }
}