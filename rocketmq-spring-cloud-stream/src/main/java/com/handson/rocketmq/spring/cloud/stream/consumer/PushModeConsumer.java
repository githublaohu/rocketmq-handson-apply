package com.handson.rocketmq.spring.cloud.stream.consumer;

import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import com.handson.rocketmq.spring.cloud.stream.UserInfo;

@Component
public class PushModeConsumer {

	@StreamListener("sync_consumer")
	public void sync(UserInfo userInfo) {
		System.out.println(String.format("topic is test-topic , tag is sync success consumer data  %s ", userInfo.toString()));
	}
	
	@StreamListener("async_consumer")
	public void async(UserInfo userInfo) {
		System.out.println(String.format("topic is test-topic , tag is async success consumer data  %s ", userInfo.toString()));
	}
}

