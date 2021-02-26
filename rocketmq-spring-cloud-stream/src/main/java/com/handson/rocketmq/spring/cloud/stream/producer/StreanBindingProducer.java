package com.handson.rocketmq.spring.cloud.stream.producer;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * 三个topic
 * @author laohu
 *
 */
public interface StreanBindingProducer {

	@Output("sync_producer")
	MessageChannel sync();

	@Output("async_producer")
	MessageChannel async();

	@Output("transaction_producer")
	MessageChannel transactional();
}
