+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "部署指南"
weight = 13
prev = "/01-start/dev-guide/"
next = "/02-guide"
+++

## 安装Java环境

请使用JDK1.7及其以上版本。[详情参见](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## 安装Zookeeper

请使用Zookeeper 3.4.6及其以上版本。[详情参见](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)。或使用elastic-job自带的内嵌Zookeeper

## 安装Maven

请使用Maven 3.0.4及其以上版本。[详情参见](http://maven.apache.org/install.html)

## 引入elastic-job

```xml
<!-- 引入elastic-job核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>

<!-- 使用springframework自定义命名空间时引入 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
