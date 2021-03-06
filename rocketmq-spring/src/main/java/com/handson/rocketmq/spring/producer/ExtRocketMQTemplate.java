package com.handson.rocketmq.spring.producer;

import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

@ExtRocketMQTemplateConfiguration(value="handsonExtRocketMQTemplate")
public class ExtRocketMQTemplate extends RocketMQTemplate {

}
