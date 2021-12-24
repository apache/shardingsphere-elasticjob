+++
title = "作业属性配置"
weight = 5
chapter = true
+++

## 简介

ElasticJob 提供属性配置的方式为不同类型的作业提供定制化配置。

## 作业类型

### 简单作业

接口名称：org.apache.shardingsphere.elasticjob.simple.job.SimpleJob

可配置属性：无

### 数据流作业

接口名称：org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob

可配置属性：

| *名称*             | *数据类型*   | *说明*         | *默认值*  |
| ----------------- | ----------- | -------------- | -------- |
| streaming.process | boolean     | 是否开启流式处理 | false    |

### 脚本作业

类型：SCRIPT

可配置属性：

| *名称*               | *数据类型*   | *说明*           | *默认值*  |
| ------------------- | ----------- | ---------------- | -------- |
| script.command.line | String      | 脚本内容或运行路径 | -        |

### HTTP作业

类型：HTTP

可配置属性：

| *名称*                               | *数据类型*   | *说明*              |  *默认值*  |
| ----------------------------------- | ----------- | ------------------ | --------  |
| http.url                            | String      | http请求url         | -         |
| http.method                         | String      | http请求方法         | -         |
| http.data                           | String      | http请求数据         | -         |
| http.connect.timeout.milliseconds   | String      | http连接超时         | 3000      |
| http.read.timeout.milliseconds      | String      | http读超时           | 5000      |
| http.content.type                   | String      | http请求ContentType  | -         |

