# spring-RocketMQ-stream

## 实验介绍
本实验，你将使用基于RocketMQ的spring-cloud-stream，实现基于spring-cloud-stream实现消息生产与消费者

通过本实验，你将学习：
*  spring-cloud-stream配置体系
*  如何实现消息生产者
*  如何实现消息消费者


## 演示官方dome

### 编译与启动
```bash

pkill -9 java

git clone https://github.com/githublaohu/rocketmq-handson-apply.git

cd rocketmq-handson-apply/rocketmq-spring-cloud-stream

mvn install -Dmaven.test.skip=true

cd ./target/

jar_original=`ls *.original`

jar_path="./"${jar_original%.original}

cp $jar_path ~/

cd  ~/

export local=$RANDOM

nohup  java -jar $jar_path   &

```

### 测试同步消息
```shell
wget -q -O- "127.0.0.1:28082/messageSend/sendSync?id=1&name=xiaoming&action=go"

tail -f -n 200 nohup.out


```

### 测试异步消息
```shell

wget -q -O- "127.0.0.1:28082/messageSend/sendAsync?id=2&name=xiaohua&action=to"

tail -f -n 200 nohup.out

```

### 测试事务消息
``` shell
# 回调提交
wget -q -O- "127.0.0.1:28082/messageSend/sendTransactional?id=1&name=xiaoming&action=go&ags=1"

tail -f -n 200 nohup.out

# 回滚事务消息
wget -q -O- "127.0.0.1:28082/messageSend/sendTransactional?id=1&name=xiaoming&action=go&ags=2"

tail -f -n 200 nohup.out

# 直接提交
wget -q -O- "127.0.0.1:28082/sendTransactional?id=1&name=xiaoming&action=go&ags=3"

tail -f -n 200 nohup.out

```




## 开发流程


#### maven依赖
```xml
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
  <version>${vision}</version>
</dependency>
```
#### 配置讲解
#{固定前缀}.#{识别标记}.#{consumer or producer or null}.#{变量}
##### 固定标记
spring固定前缀是：spring.cloud.stream.bindings
RocketMQ固定前最是： spring.cloud.stream.rocketmq


##### 识别标记
> 识别标记是配置中最要的，配置里面同样识别为一组。这组数据为一组配置，实例化一个生产者或者消费者。比如识别标记为 topic。注解Output("topic")，Input("topic")，StreamListener("topic")，就会适应对应的能力


#### 核心配置
| 配置名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| nameServer | String | 127.0.0.1:9876（必须） | nameServer地址。多服务地址配置方式host:port;host:port |
| accessKey | String | 无 | ak |
| secretKey | String | 无 | sk |
| enableMsgTrace | boolean | true | 开启消息轨迹 |
| customizedTraceTopic | String |  | 极度不建议修改。除非你修改了消息轨迹的监听目录 |



配置演示
```xml
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876
spring.cloud.stream.rocketmq.binder.access-key=access-key
spring.cloud.stream.rocketmq.binder.secret-key=secret-key
spring.cloud.stream.rocketmq.binder.enable-msg-trace=false
spring.cloud.stream.rocketmq.binder.customized-trace-topic=aaaa
```


#### spring-bind配置
| 配置名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| destination | String | 无（必须） | topic名 |
| content-type | String | 无（必须） | 数据序列化方式。如果传递是对象建议使用application/json。如果传递是string建议使用text/plain |



配置演示
```yaml
spring.cloud.stream.bindings.output1.destination=test-topic
spring.cloud.stream.bindings.output1.content-type=application/json
```




## 生产者


### 配置说明


#### 主要配置
| 配置名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| group | String | 无（必须） | 消费组 |
| maxMessageSize | int | 1024 * 1024 * 4 | 消息最大大小 |
| transactional | boolean | false | 是否是事务消息 |
| sync | boolean | false | 是否是同步消息 |
| vipChannelEnabled | boolean | true |  |
| sendMessageTimeout | int | 3000 | 发送消息超时时间 |
| compressMessageBodyThreshold | int | 1024 * 4 | 消息超过该值就压缩 |
| retryTimesWhenSendFailed | int | 2 | 同步模式，返回发送消息失败前内部重试发送的最大次数。可能导致消息重复 |
| retryTimesWhenSendAsyncFailed | int | 2 | 异步模式，返回发送消息失败前内部重试发送的最大次数。可能导致消息重复 |
| retryNextServer | boolean | false | 是否使用broker备用端口号 |



> 关于output1详细讲解下

```yaml
spring.cloud.stream.bindings.sync_producer.destination=test-topic
spring.cloud.stream.bindings.sync_producer.content-type=application/json
spring.cloud.stream.rocketmq.bindings.sync_producer.producer.group=sync-group-producer
spring.cloud.stream.rocketmq.bindings.sync_producer.producer.sync=true
```




#### 发送配置
> 在message构建时写入到setHeader里面


```java
Message message = MessageBuilder.withPayload(msg)
				.setHeader(MessageConst.PROPERTY_TAGS, tag)
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
				.build();
```



| 配置名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| MessageConst.PROPERTY_TAGS | String | 无 | 标签 |
| MessageConst.PROPERTY_DELAY_TIME_LEVEL | int | 0 | 压缩级别 |
| BinderHeaders.PARTITION_HEADER | boolean | false | 如何是true，使用syncSendOrderly方法发送消息 |
| RocketMQBinderConstants.ROCKET_TRANSACTIONAL_ARG | Object | 无 | 发送事务消息的时候使用，作用于事务消息处理器的处理 |

#### 
### 发送流程演示


#### 第一步 配置RocketMQ-binder 核心配置
```xml
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876
spring.cloud.stream.rocketmq.binder.access-key=access-key
spring.cloud.stream.rocketmq.binder.secret-key=secret-key
spring.cloud.stream.rocketmq.binder.enable-msg-trace=false
spring.cloud.stream.rocketmq.binder.customized-trace-topic=aaaa
```
#### 第二步 配置发送对象

```xml
spring.cloud.stream.bindings.sync_producer.destination=test-topic
spring.cloud.stream.bindings.sync_producer.content-type=application/json
spring.cloud.stream.rocketmq.bindings.sync_producer.producer.group=sync-group-producer
spring.cloud.stream.rocketmq.bindings.sync_producer.producer.sync=true

spring.cloud.stream.binding.no_sync_producer.destination=test-topic
spring.cloud.stream.bindings.no_sync_producer.content-type=application/json
spring.cloud.stream.rocketmq.bindings.no_sync_producer.producer.group=no-sync-group-producer
spring.cloud.stream.rocketmq.bindings.no_sync_producer.producer.sync=true

spring.cloud.stream.bindings.async_producer.destination=test-topic
spring.cloud.stream.bindings.async_producer.content-type=application/json
spring.cloud.stream.rocketmq.bindings.async_producer.producer.group=async-group-producer
spring.cloud.stream.rocketmq.bindings.async_producer.producer.sync=false

spring.cloud.stream.bindings.transaction_producer.destination=transactionTopic
spring.cloud.stream.bindings.transaction_producer.content-type=application/json
spring.cloud.stream.rocketmq.bindings.transaction_producer.producer.transactional=true
spring.cloud.stream.rocketmq.bindings.transaction_producer.producer.group=transactional-producer

```
#### 第三步 创建发送对象
```java
public interface StreanBindingProducer {

	@Output("sync_producer")
	MessageChannel sync();

	@Output("async_producer")
	MessageChannel async();

	@Output("transaction_producer")
	MessageChannel transactional();
}
```


第三步，另外一种加强的方式
> 对发送对象进行封装，优势是代码简单，有模块层次感。如果方法多个地方调用可以提高代码可维护性。

细节
1. 在封装类上必须标记@Component注解或者子注解
2. 引入有Output注解的类

```java
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
```

#### 
#### 第四步 配置发送对象
> 建议在spring-boot的启动类上使用EnableBinding注解

```java

@SpringBootApplication
// 可以配置多个
@EnableBinding({ StreanBindingConsumer.class, StreanBindingProducer.class })
public class RocketMQApplication {

	public static void main(String[] args) {
		SpringApplication.run(RocketMQApplication.class, args);
	}

}
```


##### 第五部 调用发送对象
```java
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

```

##### 消息事务
```java
@RocketMQTransactionListener(txProducerGroup = "transactional-producer", corePoolSize = 5,
		maximumPoolSize = 10)
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

	@PostConstruct
	public void init() {
		System.out.println("RocketMQLocalTransactionListener start");
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public RocketMQLocalTransactionState executeLocalTransaction(Message msg,
			Object arg) {
		Object num = msg.getHeaders().get(RocketMQBinderConstants.ROCKET_TRANSACTIONAL_ARG);

		if ("1".equals(num)) {
			System.out.println(
					"executer: " + new String((byte[]) msg.getPayload()) + " unknown");
			return RocketMQLocalTransactionState.UNKNOWN;
		}
		else if ("2".equals(num)) {
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
```

## 消费者
### 配置说明
#### 主要配置
| 配置名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| orderly | boolean | false | true 循序消费 |
| broadcasting | boolean | false | 是否使用广播模式 |
| sql | String | null | SelectorType模式为sql |
| tags | String | null | SelectorType模式为tags |





#### spring-bind配置
> **一定要注意消费者的消费组，在spring bind配置里面**
> **一定要注意消费者的消费组，在spring bind配置里面**
> **一定要注意消费者的消费组，在spring bind配置里面**
> **一定要注意消费者的消费组，在spring bind配置里面**
> **一定要注意消费者的消费组，在spring bind配置里面**
> **一定要注意消费者的消费组，在spring bind配置里面**

**

| 配置名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| group | String | 无（必须） | 消费组 |
| concurrency | int | 1 | 消费线程数 |
| max-attempts | int | 3 | 消费失败，spring重试次数。不建议使用,rocketmq默认有重试机制 |
| back-off-initial-interval | int | 100(毫秒) | 第一次重试间隔 |
| back-off-multiplier | int | 2 | 重试时间因子。重试时间=back-off-initial-interval*back-off-multiplier |





### 消费流程演示
#### 第一步 配置RocketMQ-binder 核心配置
```xml
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876
spring.cloud.stream.rocketmq.binder.access-key=access-key
spring.cloud.stream.rocketmq.binder.secret-key=secret-key
spring.cloud.stream.rocketmq.binder.enable-msg-trace=false
spring.cloud.stream.rocketmq.binder.customized-trace-topic=aaaa
```
#### 第二步 配置发送对象

```xml
spring.cloud.stream.bindings.sync_consumer.destination=test-topic
spring.cloud.stream.bindings.sync_consumer.content-type=application/json
spring.cloud.stream.bindings.sync_consumer.group=sync-group-consumer
spring.cloud.stream.rocketmq.bindings.sync_consumer.consumer.tags=sync
spring.cloud.stream.rocketmq.bindings.sync_consumer.consumer.orderly=true

spring.cloud.stream.bindings.async_consumer.destination=test-topic
spring.cloud.stream.bindings.async_consumer.content-type=application/json
spring.cloud.stream.bindings.async_consumer.group=async-group-consumer
spring.cloud.stream.bindings.async_consumer.consumer.concurrency=20
spring.cloud.stream.bindings.async_consumer.consumer.maxAttempts=1
spring.cloud.stream.rocketmq.bindings.async_consumer.consumer.orderly=false
spring.cloud.stream.rocketmq.bindings.async_consumer.consumer.tags=async

spring.cloud.stream.bindings.transaction_consumer.destination=transactionTopic
spring.cloud.stream.bindings.transaction_consumer.content-type=application/json
spring.cloud.stream.bindings.transaction_consumer.group=transaction-group-consumer
spring.cloud.stream.bindings.transaction_consumer.consumer.concurrency=5

```
#### 第三步 创建消费对象

##### 创建消费接口
> 必须创建消费接口，@StreamListener只读取@Input的配置，比如@StreamListener("xxxx") 里面的xxxx不存在Input里面，直接无效。

* 建议命名方式 {业务作用}StreanBindingConsumer

```java
public interface StreanBindingConsumer {

	@Input("sync_consumer")
	SubscribableChannel sync();

	@Input("async_consumer")
	SubscribableChannel async();
	
    @Input("transaction_consumer")
    PollableMessageSource transaction(); 
	
}

```

##### push模式
> 在方法上使用StreamListener注解表示方法是消费

* 建议命名方式 {业务作用}PushModeConsumer
```java
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
```


##### pull模式
创建pull对象
* 返回对象一定是PollableMessageSource
* 返回对象一定是PollableMessageSource
* 返回对象一定是PollableMessageSource
* 建议一个线程执行
* 建议在无限循环（while(true) or for(;;)）调用pull方法
* 建议命名方式 {业务作用}PushModeConsumer
```java
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
```