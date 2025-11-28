package com.nc.ums.service;

import java.util.Map;

public interface MessageService {
	 boolean enqueueMessage(String messageId, Map<String,Object> payload);
}
