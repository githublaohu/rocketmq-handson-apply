package com.handson.rocketmq.spring.cloud.stream.consumer;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.messaging.SubscribableChannel;


public interface StreanBindingConsumer {

	@Input("sync_consumer")
	SubscribableChannel sync();

	@Input("async_consumer")
	SubscribableChannel async();
	
    @Input("transaction_consumer")
    PollableMessageSource transaction(); 
	
}
