package com.handson.rocketmq.spring.producer;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;


@RocketMQTransactionListener
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

	@SuppressWarnings("rawtypes")
	@Override
	public RocketMQLocalTransactionState executeLocalTransaction(Message msg,
			Object arg) {

		if ("1".equals(arg)) {
			System.out.println(
					"executer: " + new String((byte[]) msg.getPayload()) + " unknown");
			return RocketMQLocalTransactionState.UNKNOWN;
		}
		else if ("2".equals(arg)) {
			System.out.println(
					"executer: " + new String((byte[]) msg.getPayload()) + " rollback");
			return RocketMQLocalTransactionState.ROLLBACK;
		}
		System.out.println(
				"executer: " + new String((byte[]) msg.getPayload()) + " commit");
		return RocketMQLocalTransactionState.COMMIT;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
		System.out.println("check: " + new String((byte[]) msg.getPayload()));
		return RocketMQLocalTransactionState.COMMIT;
	}

}
