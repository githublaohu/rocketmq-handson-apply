package com.handson.rocketmq.spring.consumer;

import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.handson.rocketmq.spring.UserInfo;

@Component
@RocketMQMessageListener(topic = "${topic.broadcastConsumer}", consumerGroup = "${group.consumer.broadcastConsumer}",messageModel= MessageModel.BROADCASTING)
public class FirstBroadcastConsumer implements RocketMQListener<UserInfo> {

	@Override
	public void onMessage(UserInfo message) {
		System.out.println(String.format("consumer name is %s data is %s",this.getClass().getSimpleName(), message));
	}

}
