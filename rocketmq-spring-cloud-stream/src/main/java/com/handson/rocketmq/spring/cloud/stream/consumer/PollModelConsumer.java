package com.handson.rocketmq.spring.cloud.stream.consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import com.handson.rocketmq.spring.cloud.stream.UserInfo;

@Component
public class PollModelConsumer {

	@Autowired
	private StreanBindingConsumer streanBindingConsumer;

	@PostConstruct
	private void init() {
		new Thread(new PollConsumerThreand()).start();
	}

	class PollConsumerThreand implements Runnable {

		@Override
		public void run() {
			System.out.println("poll mode consumer message thread start");
			while (true) {
				try {
					while (true) {
						PollModelConsumer.this.streanBindingConsumer.transaction().poll(new MessageHandler() {

							@Override
							public void handleMessage(Message<?> message) throws MessagingException {
								System.out.println(String.format("topic is transactionTopic , success consumer data  %s ", message.getPayload().toString()));
								
							}
						}, new ParameterizedTypeReference<UserInfo>() {
						});

					}
				} catch (Exception e) {

				}
			}

		}

	}
}
