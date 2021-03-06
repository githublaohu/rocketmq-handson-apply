package com.handson.rocketmq.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQLocalRequestCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.handson.rocketmq.spring.producer.ExtRocketMQTemplate;

@RestController
@RequestMapping("/messageSend")
public class MessageSendController {

	@Autowired
	private RocketMQTemplate rocketMQTemplate;

    @Autowired
	@Qualifier("extRocketMQTemplate")
	private ExtRocketMQTemplate extRocketMQTemplate;

	@Value("${topic.sendDataType}")
	private String sendDataType;

	@Value("${topic.sendOperationType}")
	private String sendOperationType;

	@Value("${topic.sendAndReceive}")
	private String sendAndReceive;

	@Value("${topic.broadcastConsumer}")
	private String broadcastConsumer;

	@Value("${topic.syncSendOrderl}")
	private String syncSendOrderly;
	
	@Value("${topic.transactionMessage}")
	private String transactionMessage;
	
	@Value("${tag.syscSendOrderl.ordinary}")
	private String ordinary;
	
	@Value("${tag.syscSendOrderl.generic}")
	private String generic;
	
	

	@GetMapping("/sendObject")
	public SendResult sendObject(UserInfo userInfo) {
		userInfo.setOperation("sendObject");
		return getRocketMQTemplate(userInfo).syncSend(sendDataType, userInfo);
	}

	@GetMapping("/sendConvertData")
	public boolean sendConvertData(UserInfo userInfo) {
		userInfo.setOperation("sendConvertData");
		getRocketMQTemplate(userInfo).convertAndSend(sendDataType, userInfo);
		return true;
	}

	@GetMapping("/sendMessage")
	public SendResult sendMessage(UserInfo userInfo) {
		userInfo.setOperation("sendMessage");
		Message<UserInfo> message = MessageBuilder.withPayload(userInfo).setHeader(RocketMQHeaders.KEYS,
				System.currentTimeMillis()).build();
		return getRocketMQTemplate(userInfo).syncSend(sendDataType, message);
	}

	@GetMapping("/sendCollection")
	public SendResult sendCollection(UserInfo userInfo) {
		userInfo.setOperation("sendCollection");
		List<Message<UserInfo>> messageList = new ArrayList<>();
		for(int i = 0 ; i <5; i++) {
			UserInfo newUserInfo = new UserInfo();
			newUserInfo.setId(userInfo.getId() + i);
			newUserInfo.setAction(userInfo.getAction());
			newUserInfo.setName(userInfo.getName());
			newUserInfo.setOperation(userInfo.getOperation());
			messageList.add(MessageBuilder.withPayload(newUserInfo).setHeader(RocketMQHeaders.KEYS,
				System.currentTimeMillis()).build());
		}
		return getRocketMQTemplate(userInfo).syncSend(sendDataType, messageList);
	}

	@GetMapping("syncSend")
	public SendResult syncSend(UserInfo userInfo) {
		userInfo.setOperation("syncSend");
		return getRocketMQTemplate(userInfo).syncSend(sendOperationType, userInfo);
	}

	@GetMapping("asyncSend")
	public boolean asyncSend(UserInfo userInfo) {
		userInfo.setOperation("asyncSend");
		getRocketMQTemplate(userInfo).asyncSend(sendOperationType, userInfo, new SendCallback() {

			@Override
			public void onSuccess(SendResult sendResult) {
				System.out.println(String.format("producer  topic is %s message is %s", sendOperationType , sendResult	));
			}

			@Override
			public void onException(Throwable e) {

			}
		});
		return true;
	}
	
	@GetMapping("oneWaySend")
	public boolean oneWaySend(UserInfo userInfo) {
		userInfo.setOperation("oneWaySend");
		getRocketMQTemplate(userInfo).sendOneWay(sendOperationType, userInfo);
		return true;
	}

	@GetMapping("transactionMessage")
	public SendResult transactionMessage(UserInfo userInfo , int arg) {
		userInfo.setOperation("transactionMessage");
		return getRocketMQTemplate(userInfo).sendMessageInTransaction(transactionMessage, MessageBuilder.withPayload(userInfo).setHeader(RocketMQHeaders.KEYS,
				System.currentTimeMillis()).build(), arg);
	}

	@GetMapping("syncSendOrderly")
	public boolean syncSendOrderly(UserInfo userInfo) {
		userInfo.setOperation("syncSendOrderly");
		getRocketMQTemplate(userInfo).syncSend(syncSendOrderly, userInfo,userInfo.getId());
		return true;
	}

	@GetMapping("sendAndReceive")
	public OperationInfo sendAndReceive(UserInfo userInfo) {
		userInfo.setOperation("sendAndReceive");
		return getRocketMQTemplate(userInfo).sendAndReceive(sendAndReceive+":"+ordinary, userInfo, OperationInfo.class);
	}

	@GetMapping("sendAndReceiveToGeneric")
	public ResultObject<OperationInfo> sendAndReceiveToGeneric(UserInfo userInfo) {
		userInfo.setOperation("sendAndReceiveToGeneric");
		return getRocketMQTemplate(userInfo).sendAndReceive(sendAndReceive+":"+generic, userInfo,
				new TypeReference<ResultObject<OperationInfo>>() {}.getType());
	}

	@GetMapping("sendAndReceiveToAsync")
	public boolean sendAndReceiveToAsync(UserInfo userInfo) {
		userInfo.setOperation("sendAndReceiveToAsync");
		getRocketMQTemplate(userInfo).sendAndReceive(sendAndReceive+":"+generic, userInfo,
				new RocketMQLocalRequestCallback<ResultObject<OperationInfo>>() {

					@Override
					public void onSuccess(ResultObject<OperationInfo> message) {
						System.out.println(String.format("producer  topic is %s tag is %s message is %s", sendAndReceive , generic , message));
					}

					@Override
					public void onException(Throwable e) {

					}
				});
		return true;
	}

	@GetMapping("broadcastConsumer")
	public SendResult broadcastConsumer(UserInfo userInfo) {
		userInfo.setOperation("broadcastConsumer");
		return getRocketMQTemplate(userInfo).syncSend(broadcastConsumer, userInfo);
	}
	


	private RocketMQTemplate getRocketMQTemplate(UserInfo userInfo) {
		return rocketMQTemplate;
	}

}
