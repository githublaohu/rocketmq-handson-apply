package com.handson.rocketmq.spring.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQReplyListener;
import org.springframework.stereotype.Component;

import com.handson.rocketmq.spring.OperationInfo;
import com.handson.rocketmq.spring.ResultObject;
import com.handson.rocketmq.spring.UserInfo;

@Component
@RocketMQMessageListener(topic = "${topic.sendAndReceive}", consumerGroup = "${group.consumer.sendAndReceiveGeneric}",selectorExpression="${tag.syscSendOrderl.generic}")
public class ReceiveGenericConsumer implements RocketMQReplyListener<UserInfo,ResultObject<OperationInfo>>{

	@Override
	public ResultObject<OperationInfo> onMessage(UserInfo message) {
		System.out.println(String.format("consumer name is %s data is %s",this.getClass().getSimpleName(), message));
		OperationInfo operationInfo = new OperationInfo();
		operationInfo.setOperation(message.getOperation());
		operationInfo.setUserInfoId(message.getId().toString());
		operationInfo.setOperationTime(System.currentTimeMillis());
		ResultObject<OperationInfo>  resultObject = new ResultObject<>();
		resultObject.setCode(200);
		resultObject.setMessage("success");
		resultObject.setData(operationInfo);
		return resultObject; 
	}

}
