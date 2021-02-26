package com.handson.rocketmq.spring.cloud.stream.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import com.handson.rocketmq.spring.cloud.stream.UserInfo;

@Component
public class EncapsulationOutput {

	@Autowired
	private StreanBindingProducer streanBindingProducer;
	
	
	public void syncsend(UserInfo userInfo) {
		userInfo.setOperation("sync");
		Map<String, Object> headersData = new HashMap<>();
		headersData.put(MessageConst.PROPERTY_TAGS, "sync");
		headersData.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 5);
		Message<UserInfo> message = MessageBuilder.createMessage(userInfo, new MessageHeaders(headersData));
		streanBindingProducer.sync().send(message);
	}

	public void asyncsend(UserInfo userInfo) {
		userInfo.setOperation("async");
		Map<String, Object> headersData = new HashMap<>();
		headersData.put(MessageConst.PROPERTY_TAGS, "async");
		headersData.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 5);
		Message<UserInfo> message = MessageBuilder.createMessage(userInfo, new MessageHeaders(headersData));
		streanBindingProducer.async().send(message);
	}

	public void transactional(UserInfo userInfo, int ags) {
		userInfo.setOperation("transactional");
		Map<String, Object> headersData = new HashMap<>();
		headersData.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 5);
		headersData.put(RocketMQBinderConstants.ROCKET_TRANSACTIONAL_ARG, ags);
		Message<UserInfo> message = MessageBuilder.createMessage(userInfo, new MessageHeaders(headersData));
		streanBindingProducer.transactional().send(message);
	}
}
