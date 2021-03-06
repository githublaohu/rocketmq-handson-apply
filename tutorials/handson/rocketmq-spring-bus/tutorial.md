# spring-RocketMQ-bus

## 实验介绍
本实验，你将使用基于RocketMQ的spring-cloud-bus，实现基于spring-cloud集群内的远程事件广播通知。

通过本实验，你将学习：
* 创建并发送远程事件
* 接收远程事件
* 处理远程事件的ACK回调


### bus[消息总线介绍]
在微服务中的架构中，我们通常用轻量级的消息代理来构建一个公用的消息主题。消息主题的目的是让系统中所有微服务的实例都链接起来。在这个消息代理中，该消息主题(公用的)所产生的消息会被所有的实例监听和消费，我们将这整个过程叫做消息总线。

Spring Cloud Bus 是用来将分布式系统的节点与轻量级消息系统链接起来的框架， 它整合了 JAVA 的时间处理机制和消息中间件的功能。SpringCloud Bus 能管理和传播分布式系统间的消息，就像一个分布式执行器，可用于广播状态更改，事件推送等，也可以当做微服务间的通信通道。

比如集群中有3个服务，分别是A，B，C。A发送了一个远程事件，A，B，C三个服务都会收到这个远程事件，接着处理对应业务。
下图简单的演示了当配置发现修改的时候，如果通知集群同步更新配置。serviceA-3接受通知页面更新了配置，马上发送配置修改的事件消息到bus里面。serviceA-1,serciceA-2注册了修改配置事件，收到修改配置事件。重新拉取配置。


![1202638-20180521203126866-1299643942.png](https://cdn.nlark.com/yuque/0/2021/png/1509048/1613234725918-604aa261-8ea7-4a62-baff-43db475a379f.png#align=left&display=inline&height=580&margin=%5Bobject%20Object%5D&name=1202638-20180521203126866-1299643942.png&originHeight=580&originWidth=968&size=70094&status=done&style=none&width=968)



## 演示官方dome

### 编译与启动
```bash

pkill -9 java

git clone https://github.com/alibaba/spring-cloud-alibaba.git

cd spring-cloud-alibaba

mvn install -Dmaven.test.skip=true

cd ./spring-cloud-alibaba-examples/spring-cloud-bus-rocketmq-example/target/

jar_original=`ls *.original`

jar_path="./"${jar_original%.original}

cp $jar_path ~/

cd  ~/

nohup  java -jar $jar_path &

```
### 测试

#### 测试一
```shell

# 发送请求
wget 127.0.0.1:8888/bus/event/publish/user?name=RocketMQ2

#查看日志打印
tail -f -n 200 nohup.out

# 广播信息的打印
Server [port : 8888] listeners on User{id=1613788852664, name='RocketMQ2'}
# ack回调信息
Server [port : 8888] listeners on {"type":"AckRemoteApplicationEvent","timestamp":1613788852845,"originService":"spring-cloud-bus-rocketmq-example:8888","destinationService":"**","id":"1f0901e2-fa50-483a-ac4c-d3b79f1bd5b3","ackId":"3fde7964-21bd-4c68-a942-8d9b1b678be5","ackDestinationService":"**","event":"com.alibaba.cloud.examples.rocketmq.UserRemoteApplicationEvent"}
```


#### 测试二
```shell
# 发送请求
wget 127.0.0.1:8888/bus/event/publish/user?name=RocketMQ&destination=bus
#查看日志打印
tail -f -n 200 nohup.out

# 广播信息的打印
Server [port : 8888] listeners on User{id=1613788060560, name='RocketMQ'}

```
> 第二次测试没有触发回调，原因是传递destination参数。至于为什么没有触发这里不详细说明。有兴趣的请看org.springframework.cloud.bus.ServiceMatcher.isForSelf方法与AntPathMatcher类

## 实现流程
#### maven配置
```xml
<dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-bus-rocketmq</artifactId>
  		<version>${revision}</version>
</dependency>
```


#### 配置
> 在spring-boot配置里面加入下面两行配置，就可以启动spring-RocketMQ-bus

```java
// web服务端口
server.port=8080
// 启动trace级别日志
spring.cloud.bus.trace.enabled=true
// spring.cloud.bus.id 是设定这个实例的在进群唯一标识符。建议使用下面的规则。${项目名}:${服务端口}
// 也可以使用dubbo的端口
// 最好是本机ip地址
spring.cloud.bus.id=${spring.application.name}-{IP}:${server.port}
// 配置spring-cloud-bus需要的stream实现，使用了rocketmq
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876

```
#### 使用RemoteApplicationEventScan注解启动bus
> com.alibaba.cloud.examples.rocketmq 事件的目录

```java
@RestController
@EnableAutoConfiguration
@RemoteApplicationEventScan(basePackages = "com.alibaba.cloud.examples.rocketmq")
public class RocketMQBusApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(RocketMQBusApplication.class).properties("IP").run(args);
	}
}
```
#### 创建消息总线事件


>  创建的事件必须继承RemoteApplicationEvent，
>  创建的事件必须继承RemoteApplicationEvent
>  创建的事件必须继承RemoteApplicationEvent

```java
package com.alibaba.cloud.examples.rocketmq;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

/**
 * {@link User} {@link RemoteApplicationEvent}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.1
 */
public class UserRemoteApplicationEvent extends RemoteApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User user;

	public UserRemoteApplicationEvent() {
	}

	public UserRemoteApplicationEvent(Object source, User user, String originService,
			String destinationService) {
		super(source, originService, destinationService);
		this.user = user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
```
#### 创建消息


#### 创建发送对象
> 使用ApplicationEventPublisher对象发送事件。直接调用publishEvent方法传递需要广播的事件

```java
	@Autowired
	private ApplicationEventPublisher publisher;

	/**
	 * Publish the {@link UserRemoteApplicationEvent}.
	 * @param name the user name
	 * @param destination the destination
	 * @return If published
	 */
	@GetMapping("/bus/event/publish/user")
	public boolean publish(@RequestParam String name,
			@RequestParam(required = false) String destination) {
		User user = new User();
		user.setId(System.currentTimeMillis());
		user.setName(name);
		publisher.publishEvent(new UserRemoteApplicationEvent(this, user, originService, destination));
		return true;
	}
```




#### 接受方法
> 1. 在方法上使用EventListener注解，方法形参是接受的事件就行了。
> 1. class上必须加上Component注解或者它的子注解（Controller，Service，Repository，RestController）
> 1. AckRemoteApplicationEvent事件触发点是发送事件的服务收到消息总线广播的广播就会触发AckRemoteApplicationEvent事件，建议大家处理AckRemoteApplicationEvent事件，实现重复机制。

```java
@Server
public class EventHandler{

    @EventListener
    public void onEvent(UserRemoteApplicationEvent event) {
        System.out.printf("Server [port : %d] listeners on %s\n", localServerPort,event.getUser());
    }

    @EventListener
    public void onAckEvent(AckRemoteApplicationEvent event)throws JsonProcessingException {
        System.out.printf("Server [port : %d] listeners on %s\n", localServerPort,objectMapper.writeValueAsString(event));
    }
}
```


#### 源码及本地测试
> 请直接打开RocketMQBusApplication.java。并且执行RocketMQBusApplication的main方法

