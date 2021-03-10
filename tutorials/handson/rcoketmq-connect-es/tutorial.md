# RocketMQ-connect

## 实验介绍
本实验，你将实现不同数据源的数据同步到Elasticsearch。

通过本实验，你将学习：
* 在RocketMQ-connect集成Elasticsearch
* 完成从日志同步到Elasticsearch
* 完成从MySQL（模式）同步到Elasticsearch

### connect-elasticsearch基本设计
> 所有的数据源基本都有增加，删除，修改操作。其中关系型数据与文档数据库会有比较复杂的聚合操作。设计了一套兼容大部分数据源的mode体系



## 演示dome

### 编译与启动

### 启动模拟source服务

```bash

pkill -9 java

git clone https://github.com/githublaohu/rocketmq-handson-apply.git

cd rocketmq-handson-apply

mvn install -Dmaven.test.skip=true

cd ./rocketmq-connect-data/target/

jar_original=`ls *.original`

jar_path="./"${jar_original%.original}

cp $jar_path ~/

cd  ~/

export local=$RANDOM

nohup  java -jar $jar_path   &
```

### 安装Elasticsearch
```bash

wget https://handson-apply.oss-cn-hangzhou.aliyuncs.com/rocketmq/elasticsearch-7.8.0-linux-x86_64.tar.gz

tar -zxvf elasticsearch-7.8.0-linux-x86_64.tar.gz

nohup ./elasticsearch-7.8.0-linux-x86_64/bin/elasticsearch >/dev/null 2>&1 &

```

### 启动connect
```bash
wget https://handson-apply.oss-cn-hangzhou.aliyuncs.com/rocketmq/rocketmq-connect-runtime.tar.xz

tar -xvJf rocketmq-connect-runtime.tar.xz

cp ./connect.conf  ./conf/

wget  https://handson-apply.oss-cn-hangzhou.aliyuncs.com/rocketmq/rocketmq-connect-es-0.0.1-SNAPSHOT-jar-with-dependencies.jar

cp ./rocketmq-connect-es-0.0.1-SNAPSHOT-jar-with-dependencies.jar  ./plugins/

sh   run_worker.sh
```

### 简单处理
### 发送处理数据
```bash

curl -H "Content-type: application/json" -X POST -d '{"id":1,"phone":"13521389587","password":"test"}' 127.0.0.1:28082/connect/createData/log/log

```



### 启动sink connector
```bash
wget  -q -O- 'http://127.0.0.1:8081/connectors/test-es-single-sink
?config={
"topicNames":"log",
"connector-class": "org.apache.rocketmq.connect.es.connector.EsSinkConnector",
"source-record-converter":"org.apache.rocketmq.connect.runtime.converter.JsonConverter",
"defaultClient.serverAddress": "http://127.0.0.1:9200",
"mapper[0].mapperName":"log",
"mapper[0].tableName":"log",
"mapper[0].namingMethod":"FIELDNAME",
"mapper[0].uniqueName":"id",
"mapper[0].index":"log"
}'
```

#### 查看启动成功
```bash

wget  -q -O-  http://127.0.0.1:8081/connectors/test-es-single-sink/config

```

### 操作结果
```bash
wget  -q -O- http://127.0.0.1:9200/log/_doc/1
```

### 简单修改
``` bash
curl -H "Content-type: application/json" -X POST -d '{"id":[1,1],"phone":["98765","13521389587"]}' 127.0.0.1:28082/connect/updateData/log/log
```

### 操作结果
```bash
wget  -q -O- http://127.0.0.1:9200/log/_doc/1
```

### 简单删除
``` bash
curl -H "Content-type: application/json" -X POST -d '{"id":[1,1]}' 127.0.0.1:28082/connect/deleteData/log/log
```

### 操作结果
```bash
wget  -q -O- http://127.0.0.1:9200/log/_doc/1
```

### 数据关联

老师（teacher），学生（student），教室（classroom）三个实体的主键组成一个上课点实体（class_elations）。

```sql
reset master;

create database if not exists school;
use school;

drop table  student;
create table  student(
    id int not null primary key AUTO_INCREMENT,
    name varchar(128) not null,
    age int not null,
    interest varchar(128)
);

insert into student value(1, "小明",9,"打球");
insert into student value(2, "小红",10,"跳舞");
insert into student value(3, "小白",10,"看书");

drop table teacher;
create table  teacher(
    id int not null primary key AUTO_INCREMENT,
    name varchar(128) not null,
    age int not null,
    curriculum varchar(128) not null
);

insert into teacher value(1, "张老师",30,"语文");
insert into teacher value(2, "黄老师",30,"数学");

drop table classroom;
create table  classroom(
   id int not null primary key AUTO_INCREMENT,
   name varchar(128) not null,
   address varchar(128) not null
);
insert into classroom value(1, "一年级一 班","仁爱教学楼一楼1室");

drop table if exists class_elations;
create table  class_elations(
    id int not null primary key AUTO_INCREMENT,
    student_id int not null,
    teacher_id int not null,
    classroom_id int not null
);

insert into class_elations value(1,1,1,1);
insert into class_elations value(2,1,2,1);
insert into class_elations value(3,2,1,1);
insert into class_elations value(4,2,2,1);
```



```bash

wget  -q -O- 'http://127.0.0.1:8081/connectors/test-es-sink-join
?config={
  "connector-class": "org.apache.rocketmq.connect.es.connector.EsSinkConnector",
  "topicNames": "student,classroom,teacher,class_elations",
  "defaultClient.serverAddress": "http://127.0.0.1:9200",
  "sinkclient[0].name": "student",
    "sinkclient[0].serverAddress": "http://127.0.0.1:9200",
    "sinkclient[1].name": "main",
    "sinkclient[1].serverAddress": "http://127.0.0.1:9200",
    "mapper[0].mapperName":"student",
    "mapper[0].tableName":"student",
    "mapper[0].clientName":"student",
    "mapper[0].namingMethod":"FIELDNAME",
    "mapper[0].uniqueName":"id",
    "mapper[0].index":"student",
    "mapper[0].idPrefix":"student",
    "mapper[1].mapperName":"classroom",
    "mapper[1].tableName":"classroom",
    "mapper[1].clientName":"classroom",
    "mapper[1].namingMethod":"FIELDNAME",
    "mapper[1].uniqueName":"id",
    "mapper[1].index":"classroom",
    "mapper[2].mapperName":"teacher",
    "mapper[2].tableName":"teacher",
    "mapper[2].clientName":"teacher",
    "mapper[2].namingMethod":"FIELDNAME",
    "mapper[2].uniqueName":"id",
    "mapper[2].index":"teacher",
    "mapper[3].mapperName":"class_elations",
    "mapper[3].tableName":"class_elations",
    "mapper[3].clientName":"class_elations",
    "mapper[3].namingMethod":"FIELDNAME",
    "mapper[3].uniqueName":"id",
    "mapper[3].index":"class_elations",
  "relation[0].relationName":"class_elations",
    "relation[0].mainMapperConfig.mapperName":"class_elations",
     "relation[0].mainMapperConfig.index":"class_elations_join",
    "relation[0].fromMapperConfig[0].mapperName":"classroom",
    "relation[0].fromMapperConfig[0].mainRelationField":"classroom_id",
    "relation[0].fromMapperConfig[0].fieldAndKeyMapper.name":"classroom_name",
    "relation[0].fromMapperConfig[0].fieldAndKeyMapper.address":"classroom_address",
    "relation[0].fromMapperConfig[0].excludeField[0]":"id",
    "relation[0].fromMapperConfig[1].mapperName":"teacher",
    "relation[0].fromMapperConfig[1].mainRelationField":"teacher_id",
    "relation[0].fromMapperConfig[1].fieldAndKeyMapper.name":"teacher_name",
    "relation[0].fromMapperConfig[1].fieldAndKeyMapper.age":"classroom_age",
    "relation[0].fromMapperConfig[1].fieldAndKeyMapper.curriculum":"classroom_curriculum",
    "relation[0].fromMapperConfig[1].excludeField[0]":"id",
    "relation[0].fromMapperConfig[2].mapperName":"student",
    "relation[0].fromMapperConfig[2].mainRelationField":"student_id",
    "relation[0].fromMapperConfig[2].fieldAndKeyMapper.name":"student_name",
    "relation[0].fromMapperConfig[2].fieldAndKeyMapper.age":"student_age",
    "relation[0].fromMapperConfig[2].fieldAndKeyMapper.interest":"interest_curriculum",
    "relation[0].fromMapperConfig[2].excludeField[0]":"id",
  "source-record-converter": "org.apache.rocketmq.connect.runtime.converter.JsonConverter"
}'

```

#### 添加数据
```bash

curl -H "Content-type: application/json" -X POST -d '{"id":1,"name":"小明","age":10,"interest":"打球"}' 127.0.0.1:28082/connect/createData/student/student

curl -H "Content-type: application/json" -X POST -d '{"id":2,"name":"小红","age":9,"interest":"跳舞"}' 127.0.0.1:28082/connect/createData/student/student

curl -H "Content-type: application/json" -X POST -d '{"id":3,"name":"小白","age":9,"interest":"看书"}' 127.0.0.1:28082/connect/createData/student/student


curl -H "Content-type: application/json" -X POST -d '{"id":1,"name":"张老师","age":30,"curriculum":"语文"}' 127.0.0.1:28082/connect/createData/teacher/teacher

curl -H "Content-type: application/json" -X POST -d '{"id":2,"name":"黄老师","age":31,"curriculum":"数学"}' 127.0.0.1:28082/connect/createData/teacher/teacher



curl -H "Content-type: application/json" -X POST -d '{"id":1,"name":"一年级一班","address":"仁爱教学楼一楼1室"}' 127.0.0.1:28082/connect/createData/classroom/classroom



curl -H "Content-type: application/json" -X POST -d '{"id":1,"student_id":1,"teacher_id":1 ,"classroom_id":1}' 127.0.0.1:28082/connect/createData/class_elations/class_elations

curl -H "Content-type: application/json" -X POST -d '{"id":2,"student_id":1,"teacher_id":2 ,"classroom_id":1}' 127.0.0.1:28082/connect/createData/class_elations/class_elations

curl -H "Content-type: application/json" -X POST -d '{"id":3,"student_id":2,"teacher_id":1 ,"classroom_id":1}' 127.0.0.1:28082/connect/createData/class_elations/class_elations

curl -H "Content-type: application/json" -X POST -d '{"id":4,"student_id":2,"teacher_id":2 ,"classroom_id":1}' 127.0.0.1:28082/connect/createData/class_elations/class_elations

sleep 2

wget  -q -O- http://127.0.0.1:9200/student/_doc/1
wget  -q -O- http://127.0.0.1:9200/student/_doc/2
wget  -q -O- http://127.0.0.1:9200/student/_doc/3

wget  -q -O- http://127.0.0.1:9200/teacher/_doc/1
wget  -q -O- http://127.0.0.1:9200/teacher/_doc/2

wget  -q -O- http://127.0.0.1:9200/classroom/_doc/1


wget  -q -O- http://127.0.0.1:9200/class_elations/_doc/1
wget  -q -O- http://127.0.0.1:9200/class_elations/_doc/2
wget  -q -O- http://127.0.0.1:9200/class_elations/_doc/3
wget  -q -O- http://127.0.0.1:9200/class_elations/_doc/4

wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/1
wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/2
wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/3
wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/4


```

#### 修改从数据

```bash

curl -H "Content-type: application/json" -X POST -d '{"id":[1,1],"name":["小明","小明"],"age":[11,11],"interest":["看书，打球，玩耍","打球"]}' 127.0.0.1:28082/connect/updateData/student/student

wget  -q -O- http://127.0.0.1:9200/student/_doc/1
wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/1


```

#### 修改主数据
```bash

wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/4

curl -H "Content-type: application/json" -X POST -d '{"id":[4,4],"student_id":[1,2]}' 127.0.0.1:28082/connect/updateData/class_elations/class_elations

wget  -q -O- http://127.0.0.1:9200/class_elations_join/_doc/4
```

## connent-es设计详解

### 特性
支持多数据源
> 原始mapper与主mapper可以使用不同的es集群

支持分表识别
> 分库分表已经是常见的场景，所以对此场景进行支持。比如有一个mapper的table为user，实现表名为user_1,user_2..... 会主动识别为user

支持增删改查操作
> 支持原始mapper的增删改查

支持逻辑删除
> 目前大多数表都是逻辑删除，而不是物理删除。逻辑删除的字段与字段值在不同的表可能是不同。

支持field的映射
> 表名与数据展示名不一样，同样也提供field与数据展示名之间的映射。
> 支持不映射直接使用数据库字段名
> 驼峰映射。ui_id 映射成uiId
> 配置映射。 ui_id 映射成 userId

支持数据过滤
> 排除不需要同步的字段。比如表有a,b,c,d 如果不想同步c,d 进行简单的配置就行了。

支持主数据字段别名
> 比如a表有id,name,b表有id,name。映射别名为aId,aName,bId,bName。最后的数据为{ "aId":1 , aName:"name" , bId:1, bName:"name"}

支持多表数据组合
> 比如a表是主表，b,c表是从表。可以把a,b,c表组合成一条数据

支持从数据反向修改
> 比如a表为主表，b表为从表，b表某行发生修改，会修改结果同步到a表关联一行的组合数据里面

支持主数据修改，从数据感知修改
> 比如a表关联b表，a表关联b表的id发生改变，会自动同步新id的数据到组合数据里面


### mapper类型
```java
public enum ModelType {
	// 单表
	SIMPLE,
	// 主表
	MAIN,
	// 一方
	ONEWAYS,
	// 多方
	MANYWAYS;

}
```


3. 一方与多方mapper
> 从数据分为一对一的一方，一对多的多方。两者之间在修改的行为不一样。
> 1. 一方只需要通过主id操作es里面的数据，进行修改
> 1. 多方需要通过主字段中的关联字段进行查询，然后修改



4.  从操作mapper集合
> 主要用于在组合数据的时候，对单表信息进行查询



### 配置说明
#### 原始mapper
| 属性名 | 说明 | 类型 | 默认值 | 值说明 |
| --- | --- | --- | --- | --- |
| mapperName | 映射名 | String | 无(必须) |  |
| tableName | 表名 | String | 无(必须) |  |
| clientName | es客服端名 | String | 无 | 如果有默认client |
| namingMethod | 字段映射方式 | String | FIELDNAME |  |
| uniqueName | 表唯一字段 | String | 无(必须) |  |
| index | es的index名  | String | 无(必须) |  |
| logicDeleteFieldName | 逻辑删除字段 | String | 非  |  |
| logicDeleteFieldValue | 逻辑删除 | String | 无(非必须) |  |
| fieldAndKeyMapper | 映射字段 | Map | 无(非必须) |  |



#### client配置
| 属性名 | 说明 | 类型 | 默认值 | 值说明 |
| --- | --- | --- | --- | --- |
| name | clientName | String | 无(必须) | 与mapper的clientName对应 |
| serverAddress | es集群服务地址 | String | 无(必须) | http://127.0.0.1:9300,127.0.0.2:9300, |

#### 关联信息配置
| 属性名 | 说明 | 类型 | 默认值 | 值说明 |
| --- | --- | --- | --- | --- |
| relationName | 关系名 | String | 无(必须) |  |
| mainMapperConfig | 主关系配置 | MapperConfig | 无(必须) |  |
| fromMapperConfig | 从关系配置 | MapperConfig | 无(必须) |  |



##### 主映射关系配置
| 属性名 | 说明 | 类型 | 默认值 | 值说明 |
| --- | --- | --- | --- | --- |
| mapperName | 映射名 | String | 无(必须) | 与原始mapper一致 |
| clientName | es客服端名 | String | 无 | 如果有默认client |
| index | es的index名  | String | 无(必须) |  |
| fieldAndKeyMapper | 映射字段 | Map | 无(非必须) |  |


| 属性名 | 说明 | 类型 | 默认值 | 值说明 |
| --- | --- | --- | --- | --- |
| mapperName | 映射名 | String | 无(必须) | 与原始mapper一致 |
| mapperType | 关系类型 | mapperType | 无(必须) |  |
| mainRelationField | 与主的关联字段 | String | 无(必须) |  |
| fieldAndKeyMapper | 映射字段 | Map | 无(非必须) |  |
