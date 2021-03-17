
# RocketMQ-Spring

本实验，你讲学会RocketMQ-Spring使用以及不同API的细节

通过本实验，你将学习：
* 生产者配置
* 生产者创建
* send方法详解
* 消费者配置
* 消费者创建

## 演示dome

### 编译与启动

```bash
pkill -9 java

git clone https://github.com/githublaohu/rocketmq-handson-apply.git

cd rocketmq-handson-apply

mvn install -Dmaven.test.skip=true

cd ./rocketmq-spring/target/

jar_original=`ls *.original`

jar_path="./"${jar_original%.original}

cp $jar_path ~/

cd  ~/

export local=$RANDOM

nohup  java -jar $jar_path   &

```


### 消息类型测试

#### 发送对象
```shell
wget -q -O- "http://127.0.0.1:28082/messageSend/sendObject?id=1&name=xiaoming&action=go2&ags=3"

tail -f -n 200 nohup.out

consumer name is SendDataTypeConsumer data is UserInfo [id=1, name=xiaoming, action=go2, operation=sendObject]

```

#### 发动message
```shell
wget -q -O- "http://127.0.0.1:28082/messageSend/sendMessage?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is SendDataTypeConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=sendMessage]
```

#### 发送convert
```shell
wget -q -O- "http://127.0.0.1:28082/messageSend/sendConvertData?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is SendDataTypeConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=sendConvertData]
```

#### 发送List
```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/sendCollection?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is SendDataTypeConsumer data is UserInfo [id=3, name=xiaoming, action=des, operation=sendCollection]
consumer name is SendDataTypeConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=sendCollection]
consumer name is SendDataTypeConsumer data is UserInfo [id=4, name=xiaoming, action=des, operation=sendCollection]
consumer name is SendDataTypeConsumer data is UserInfo [id=6, name=xiaoming, action=des, operation=sendCollection]
consumer name is SendDataTypeConsumer data is UserInfo [id=5, name=xiaoming, action=des, operation=sendCollection]
```

### 测试不同发送行为

#### 同步发送
```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/syncSend?id=2&name=xiaoming&action=des&ags=3"

consumer name is SendOperationTypeConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=syncSend]
```

#### 异步发送
```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/asyncSend?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

producer  topic is sendOperationType message is SendResult [sendStatus=SEND_OK, msgId=7F0000010E7E18B4AAC2005DD0B00007, offsetMsgId=C0A87C0400002A9F0000000003D3A8AA, messageQueue=MessageQueue [topic=sendOperationType, brokerName=laohu, queueId=1], queueOffset=0]

consumer name is SendOperationTypeConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=asyncSend]


```

#### OneWay发送

```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/oneWaySend?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is SendOperationTypeConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=oneWaySend]

```

#### 发送事务消息

```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/transactionMessage?id=2&name=xiaoming&action=des&ags=1"


wget -q -O- "http://127.0.0.1:28082/messageSend/transactionMessage?id=2&name=xiaoming&action=des&ags=2"


wget -q -O- "http://127.0.0.1:28082/messageSend/transactionMessage?id=2&name=xiaoming&action=des&ags=3"



```

#### 发送同步RPC消息
```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/sendAndReceive?id=2&name=xiaoming&action=des&ags=3"

# 返回
{"userInfoId":"2","operation":"sendAndReceive","operationTime":1614535559266}

tail -f -n 200 nohup.out

consumer name is ReceiveConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=sendAndReceive]

```

#### 发送同步PRC消息结果为泛型
```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/sendAndReceiveToGeneric?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is ReceiveGenericConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=sendAndReceiveToGeneric]

{"code":200,"message":"success","data":{"userInfoId":"2","operation":"sendAndReceiveToGeneric","operationTime":1614536048349}}


```

#### 发送异步RPC消息

```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/sendAndReceiveToAsync?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is ReceiveGenericConsumer data is UserInfo [id=2, name=xiaoming, action=des, operation=sendAndReceiveToAsync]
producer  topic is sendAndReceive tag is generic message is ResultObject [code=200, message=success, data={userInfoId=2, operation=sendAndReceiveToAsync, operationTime=1614536220522}]


```

#### 顺序发送
```shell

wget -q -O- "http://127.0.0.1:28082/messageSend/syncSendOrderly?id=2&name=xiaoming&action=des&ags=3"

tail -f -n 200 nohup.out

consumer name is OrderlyConmumer data is UserInfo [id=2, name=xiaoming, action=des, operation=syncSendOrderly]


```

### 测试消费方式

#### 广播消费


## 细节点

### tag
> rocketmq-spring的tag设置与原生不同。现在把topic与tag合成了destination。destination=topic or topic":"tag。实例请看MessageSendController第152行。

### 关于message对象的header
> header作用是把不常用的数据存储到header，提供给其他点使用。

#### RocketMQ默认请求头
| 请求头 | 作用 | 类型 | 是否有用 |
| -- | -- |-- | -- | 
| RocketMQHeaders.KEYS | 消息key，为发送消息设定keys| string | 生效 |




## 生产者

### 创建生产者
#### 默认生产者创建
```xml
rocketmq.name-server=127.0.0.1:9876
rocketmq.producer.group=rocketmq-spring-producer
rocketmq.producer.sendMessageTimeout=10000
```

#### 自定义生产者

```java
@ExtRocketMQTemplateConfiguration(value="extRocketMQTemplate")
public class ExtRocketMQTemplate extends RocketMQTemplate {

}

public class MessageSendController {
   @Autowired
	private ExtRocketMQTemplate extRocketMQTemplate;

    @Autowired
    @Qualifier("extRocketMQTemplate")
	private  RocketMQTemplate rocketMQTemplate;
}
````

1. 继承RocketMQTemplate对象
2. 使用ExtRocketMQTemplateConfiguration 注解
3. 配置属性信息
4. 声明自定义消费者的全局变量，并使用@Autowired注解

##### 默认配置与ExtRocketMQTemplateConfiguration注解解读
| 配置名 | 作用 | 类型 | 默认值 | 是否必须 | 存在配置文件 | 默认配置名 |存在注解 | 说明 |
| -- | -- | -- | -- | -- | -- | -- | -- | -- | 
|  nameServer   | 注册服务地址    |  String    |     |   是  |  是   | rocketmq.name-server |  是   |
|  group    |  生产组 | String |  | 是  | 是  | rocketmq.producer.group | 是  | 
| sendMessageTimeout  | 发送超时  | int   | 3000  |  否 | 是 |  rocketmq.producer.send-message-timeout  | 是 |   | 
| compressMessageBodyThreshold | 数据压缩限制 | int | 1024*4 | 否 | 是 |  rocketmq.producer.compress-message-body-threshold |  | 
| retryTimesWhenSendFailed | 同步发送失败重试次数 | int | 2 | 否 | 是 |rocketmq.producer.retry-times-when-send-failed | 是 |  | 
| retryTimesWhenSendAsyncFailed | 异步发送失败重试次数 | int | 2 | 否 | 是 | rocketmq.producer.retry-times-when-send-async-fsailed |  |
| retryNextServer | 内部发送失败时重试另一个代理 | boolean | false | 否 | 是 | rocketmq.producer.retry-next-server | 是 |  |
| maxMessageSize | 消息最大字节数 | int | 4M | 否 | 是 | rocketmq.producer.max-message-size | 是 |  |  
| enableMsgTrace | 开启消息轨迹 | boolean | true | 否 | 是 | rocketmq.producer.enable-msg-trace | 是 |  |  
| customizedTraceTopic |  消息轨迹topic | String | RMQ_SYS_TRACE_TOPIC | 否 | 是 | rocketmq.producer.customized-trace-topic | 是  |  |
| accessKey | acl需要的ak | String |  | 否 | 是 |rocketmq.producer.access-key | 是 |  | 
| secretKey | acl需要的sk  | String |  | 否 | 是 |rocketmq.producer.secret-key | 是 |  |


###  消息生产

#### 发送形参说明
| 名 |　类型 | 说明  | 
| -- | -- | -- |  
| destination | String | 消息topic或者topic与tag组合 | 
| message | Message | 发送对象 |
| payload | Object | 发送对象 | -- | -- |
| timeout | long | 发送超时时长，如果没有，使用全局配置或者默认配置 | 
| delayLevel | int | 数据压缩级别 |  
| messages | 发送集合消息 | 集合内部数据只能是message或者子类 |  
| hashKey | String  |循序消费的消息需要, 用于确定消息的队列|
| sendCallback | SendCallback | 异步发送回调对象 |  
| rocketMQLocalRequestCallback | RocketMQLocalRequestCallback | 异步RPC回调对象 |  
| type | Type | 同步RPC消息返回对象 |  


#### 消息类型解读
| 消息类型 |　出处 |  是否返回SendResult | 优势 | 劣势 |
| -- | -- | -- | -- | -- |
| Object | rocketmq-spring | 是 |　简单，方便 | 扩展度低 |
| message | rocketmq-spring  | 是 | 可以扩展 |  相对object使用复杂 |
| convertAndSend | spring-cloud-steam| 否 | spring标准 |

> 三种消息都是使用convert进行数据序列化。本质上一样，不建议使用convertAndSend方式。

#### 生产方式

|  发送方式 |　循序消息 |  RPC消息 | 批量消息 | 事务消息 |
| -- | -- | -- | -- | -- |
| 同步 | syncSendOrderly | 支持（sendAndReceive） | 支持 | sendMessageInTransaction |
| 异步 | asyncSendOrderly | 支持（sendAndReceive） | 不支持 | 不支持 |
| OneWay | sendOneWayOrderly | 不支持 | 不支持 | 不支持 |


#### 事务消息

* 创建对象
* 实现@RocketMQTransactionListener 注解
* 配置@RocketMQTransactionListener注解
    * rocketMQTemplateBeanName 拦截那个 RocketMQTemplate的事务。值对应ExtRocketMQTemplateConfiguration.value
* 实现接口方法

```java
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

```


## 消费者

### 配置

| 配置名 | 作用 | 类型 | 默认值 | 是否必须  | 
| -- | -- | -- | -- | -- |
|  nameServer   | 注册服务地址    |  String    | ${rocketmq.name-server:}    |  必须 |
|  group    |  生产组 | String |  |  必须 |  在同一个进程中不能创建相同组的生产者  |
|  topic | 主题 | String |  |  必须| -- | -- | -- | 
| selectorType | 消息选择方式 | SelectorType | SelectorType.TAG | 否 | -- | -- | 
| selectorExpression | 消息选择表达式 | String | * | 否 | -- | -- | 
| consumeMode | 消费模式 | ConsumeMode | ConsumeMode.CONCURRENTLY | 否 | -- | -- | 
| messageModel | 消息模式 | MessageModel | MessageModel.CLUSTERING | 否 | -- | -- | 
| consumeThreadMax | 消费最大线程 | int | 64 | 否 | -- | -- | 
| consumeTimeout | 消费超时 | long | 15 | 否 | 分钟 | 
| enableMsgTrace | 开启消息轨迹 | boolean | true | 否  | rocketmq.producer.enable-msg-trace | -- |  -- | 
| customizedTraceTopic |  消息轨迹topic | String | ${rocketmq.consumer.customized-trace-topic:}  | 否 |
| accessKey | acl需要的ak | String | ${rocketmq.consumer.access-key:} | 否 | 是 |
| secretKey | acl需要的sk  | String | ${rocketmq.consumer.secret-key:} | 否 | 是 |

#### 配置泛型解读

##### ConsumeMode

分布为集群模式（CONCURRENTLY）与广播模式。
集群模式是同一个消费组内的实例，会平均的消费数据。比如有100条数据，消费组内有a,b,c三个实例，a消费1到30条，b消费31到80条，消费81到100条。



###  普通消费
* 创建class
* 实现RocketMQListener接口
* 申明泛型对象即业务对象
* 使用@RocketMQMessageListener注解
* 配置@RocketMQMessageListener注
    * 必须配置项入下
        * nameServer
        * group
        * topic
* 使用@Component注解或者子注解
* 实现接口方法

```java
@Component
@RocketMQMessageListener(topic = "${topic.sendDataType}",consumerGroup="${group.consumer.sendDataType}")
public class SendDataTypeConsumer implements RocketMQListener<UserInfo>{

	@Override
	public void onMessage(UserInfo message) {
		System.out.println(String.format("consumer name is %s data is %s",this.getClass().getSimpleName(), message));
	}

}
```

### 顺序消费
* 创建class
* 实现RocketMQListener接口
* 申明泛型对象即业务对象
* 使用@RocketMQMessageListener注解
* 配置@RocketMQMessageListener注
    * 必须配置项入下
        * nameServer
        * group
        * topic
        * consumeMode 设为 ConsumeMode.ORDERLY
* 使用@Component注解或者子注解
* 实现接口方法

```java
@Component
@RocketMQMessageListener(topic = "${topic.syncSendOrderly}", consumerGroup = "${group.consumer.syncSendOrderl}", consumeMode = ConsumeMode.ORDERLY)
public class OrderlyConmumer implements RocketMQListener<UserInfo>{

	@Override
	public void onMessage(UserInfo message) {
		System.out.println(String.format("consumer name is %s data is %s",this.getClass().getSimpleName(), message));
	}

}
```

### 广播消息
* 创建class
* 实现RocketMQListener接口
* 申明泛型对象即业务对象
* 使用@RocketMQMessageListener注解
* 配置@RocketMQMessageListener注
    * 必须配置项入下
        * nameServer
        * group
        * topic
        * messageModel 设为 MessageModel.BROADCASTING
* 使用@Component注解或者子注解
* 实现接口方法

```java
@Component
@RocketMQMessageListener(topic = "${topic.broadcastConsumer}", consumerGroup = "${group.consumer.broadcastConsumer}",messageModel= MessageModel.BROADCASTING)
public class FirstBroadcastConsumer implements RocketMQListener<UserInfo> {

	@Override
	public void onMessage(UserInfo message) {
		System.out.println(String.format("consumer name is %s data is %s",this.getClass().getSimpleName(), message));
	}

}
```

### RPC消息
* 创建class
* 实现RocketMQReplyListener接口
* 申明泛型对象即业务对象
* 申明返回泛型的对象
* 使用@RocketMQMessageListener注解
* 配置@RocketMQMessageListener注
    * 必须配置项入下
        * nameServer
        * group
        * topic
        * messageModel 设为 MessageModel.BROADCASTING
* 使用@Component注解或者子注解
* 实现接口方法

```java
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
```