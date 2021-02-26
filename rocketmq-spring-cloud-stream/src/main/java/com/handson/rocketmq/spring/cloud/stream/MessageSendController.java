package com.handson.rocketmq.spring.cloud.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.handson.rocketmq.spring.cloud.stream.producer.EncapsulationOutput;


@RestController
@RequestMapping("/messageSend")
public class MessageSendController {

	@Autowired
	private EncapsulationOutput encapsulationOutput; 

	
	@GetMapping("sendSync")
	public boolean sendSync(UserInfo userInfo) {
		encapsulationOutput.syncsend(userInfo);
		return true;
	}
	
	@GetMapping("sendAsync")
	public boolean sendAsync(UserInfo userInfo) {
		encapsulationOutput.asyncsend(userInfo);
		return true;
	}
	
	@GetMapping("sendTransactional")
	public boolean sendTransactional(UserInfo userInfo, int ags) {
		encapsulationOutput.transactional(userInfo, ags);
		return true;
	}
}
