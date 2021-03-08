# RocketMQ-connect

## 实验介绍
本实验，你将了解RocketMQ-connect如何实现相同和不同数据源之间的数据同步与转换以及部署与基本操作。

通过本实验，你将学习：
* 部署RocketMQ-connect
* 完成一次基于文件的数据流式传输
* 了解RocketMQ-connect相关配置与操作

### connect-run基本设计
> connect-run基于RocketMQ的广播模式，实现了去中心化设计。在connect-run架构设计中RocketMQ有些类似于注册中心，因为是去中心话原因connect-run每个节点都可以对connectors进行操作

![](https://oscimg.oschina.net/oscnet/up-5ff52b3f1c6c8d22a161c1b7fe71de87c04.png)



### 数据采集与处理基本设计
> 每个connect-run节点主要是两部分组成分别是web server与 connectors，而connectors分为source与sink。source负责数据采集，sink负责数据同步

![](https://oscimg.oschina.net/oscnet/up-e041ca5a2114c9243f016e8e2e410f6bf8b.png)



## 演示dome

### 编译与启动
```shell
pkill -9 java

mkdir -p plugs

git clone https://github.com/apache/rocketmq-externals.git

cd ./rocketmq-externals/rocketmq-connect

mvn install -Dmaven.test.skip=true

cd ./rocketmq-connect-runtime

cp ~/run_worker.sh ~/

cd ./target/

jar_original=`ls *.original`

jar_path="./"${jar_original%.original}

cp $jar_path ~/

cp ~/distribution/{lib,cinf} ./

cd  ~/

cd ./rocketmq-externals/rocketmq-connect/rocketmq-connect-sample/target/

jar_original=`ls *.original`
sample_jar = "./"${jar_original%.original}

cp $sample_jar ~/plug

sh   run_worker.sh

```

### 直接运行
```bash
wget https://handson-apply.oss-cn-hangzhou.aliyuncs.com/rocketmq/rocketmq-connect-runtime.tar.xz

tar -xvJf rocketmq-connect-runtime.tar.xz

sh   run_worker.sh

```

### 启动source connector
```bash

wget  -q -O- 'http://127.0.0.1:8081/connectors/test-file-source?config={"connector-class":"org.apache.rocketmq.connect.file.FileSourceConnector","topic":"fileTopic","filename":"~/conf/logback.conf","source-record-converter":"org.apache.rocketmq.connect.runtime.converter.JsonConverter"} '

```

#### 查看启动成功
```bash

wget  -q -O- http://127.0.0.1:8081/connectors/test-file-source/config

```


### 启动sink connector
```bash
wget  -q -O- 'http://127.0.0.1:8081/connectors/test-file-sink?config={"connector-class":"org.apache.rocketmq.connect.file.FileSinkConnector","topicNames":"fileTopic","filename":"~/logback.conf","source-record-converter":"org.apache.rocketmq.connect.runtime.converter.JsonConverter"}'
```

#### 查看启动成功
```bash

wget  -q -O-  http://127.0.0.1:8081/connectors/test-file-sink/config

```

### 操作结果
```bash

ll -s ~/logback.conf

# 如果显示文件存在，表示操作成功
```

## connent-run相关信息解读

### 

### 日志目录
日志目录在：${user.home}/logs/rocketmqconnect/connect_runtime.log
启动启动成功表示：The worker [DEFAULT_WORKER_1] boot success.

#### source connector配置说明

| key                     | nullable | default | description                                                                            |
| ----------------------- | -------- | ------- | -------------------------------------------------------------------------------------- |
| connector-class         | false    |         | 实现Connector接口的类名称（包含包名）                                                  |
| filename                | false    |         | 数据源文件名称                                                                         |
| task-class              | false    |         | 实现SourceTask类名称（包含包名）                                                       |
| topic                   | false    |         | 同步文件数据所需topic                                                                  |
| update-timestamp        | false    |         | 配置更新时间戳                                                                         |
| source-record-converter | false    |         | Full class name of the impl of the converter used to convert SourceDataEntry to byte[] |

### sink connector配置说明

| key                     | nullable | default | description                                                                            |
| ----------------------- | -------- | ------- | -------------------------------------------------------------------------------------- |
| connector-class         | false    |         | 实现Connector接口的类名称（包含包名）                                                  |
| topicNames              | false    |         | sink需要处理数据消息topics                                                             |
| task-class              | false    |         | 实现SourceTask类名称（包含包名）                                                       |
| filename                | false    |         | sink拉去的数据保存到文件                                                               |
| update-timestamp        | false    |         | 配置更新时间戳                                                                         |
| source-record-converter | false    |         | Full class name of the impl of the converter used to convert SourceDataEntry to byte[] |

```  
注：source/sink配置文件说明是以rocketmq-connect-sample为demo，不同source/sink connector配置有差异，请以具体sourc/sink connector为准
```  
### runtime配置参数说明

| key                      | nullable | default                                                                                         | description                                                                        |
| ------------------------ | -------- | ----------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| workerId                 | false    | DEFAULT_WORKER_1                                                                                | 集群节点唯一标识                                                                   |
| namesrvAddr              | false    |                                                                                                 | RocketMQ Name Server地址列表，多个NameServer地址用分号隔开                         |
| httpPort                 | false    | 8081                                                                                            | runtime提供restful接口服务端口                                                     |
| pluginPaths              | false    |                                                                                                 | source或者sink目录，启动runttime时加载                                             |
| storePathRootDir         | true     | (user.home)/connectorStore                                                                      | 持久化文件保存目录                                                                 |
| positionPersistInterval  | true     | 20s                                                                                             | source端持久化position数据间隔                                                     |
| offsetPersistInterval    | true     | 20s                                                                                             | sink端持久化offset数据间隔                                                         |
| configPersistInterval    | true     | 20s                                                                                             | 集群中配置信息持久化间隔                                                           |
| rmqProducerGroup         | true     | defaultProducerGroup                                                                            | Producer组名，多个Producer如果属于一个应用，发送同样的消息，则应该将它们归为同一组 |
| rmqConsumerGroup         | true     | defaultConsumerGroup                                                                            | Consumer组名，多个Consumer如果属于一个应用，发送同样的消息，则应该将它们归为同一组 |
| maxMessageSize           | true     | 4MB                                                                                             | RocketMQ最大消息大小                                                               |
| operationTimeout         | true     | 3s                                                                                              | Producer发送消息超时时间                                                           |
| rmqMaxRedeliveryTimes    | true     |                                                                                                 | 最大重新消费次数                                                                   |
| rmqMessageConsumeTimeout | true     | 3s                                                                                              | Consumer超时时间                                                                   |
| rmqMaxConsumeThreadNums  | true     | 32                                                                                              | Consumer客户端最大线程数                                                           |
| rmqMinConsumeThreadNums  | true     | 1           | Consumer客户端最小线程数                                           |
| allocTaskStrategy        | true     | org.apache.rocketmq.connect.<br>runtime.service.strategy.<br>DefaultAllocateConnAndTaskStrategy | 负载均衡策略类                                      |

### runtime支持JVM参数说明

| key                                             | nullable | default | description             |
| ----------------------------------------------- | -------- | ------- | ----------------------- |
| rocketmq.runtime.cluster.rebalance.waitInterval | true     | 20s     | 负载均衡间隔            |
| rocketmq.runtime.max.message.size               | true     | 4M      | Runtime限制最大消息大小 |
|[virtualNode](#virtualnode)       |true    |  1        | 一致性hash负载均衡的虚拟节点数|
|[consistentHashFunc](#consistenthashfunc)|true    |MD5Hash|一致性hash负载均衡算法实现类|

### connect操作接口说明


查看集群节点信息

http://(your worker ip):(port)/getClusterInfo

查看集群中Connector和Task配置信息

http://(your worker ip):(port)/getConfigInfo

查看当前节点分配Connector和Task配置信息

http://(your worker ip):(port)/getAllocatedInfo

查看指定Connector配置信息

http://(your worker ip):(port)/connectors/(connector name)/config

查看指定Connector状态

http://(your worker ip):(port)/connectors/(connector name)/status

停止所有Connector

http://(your worker ip):(port)/connectors/stopAll

重新加载Connector插件目录下的Connector包

http://(your worker ip):(port)/plugin/reload

从内存删除Connector配置信息（谨慎使用）

http://(your worker ip):(port)/connectors/(connector name)/delete
