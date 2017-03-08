+++
date = "2016-01-27T16:14:21+08:00"
title = "1.x 使用步骤"
weight=1005
+++

# 使用步骤

## 安装Java环境

请使用`JDK1.7`及其以上版本。[详情参见](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## 安装Zookeeper

请使用`Zookeeper 3.4.6`及其以上版本。[详情参见](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)。或使用`elastic-job`自带的内嵌`Zookeeper`

## 安装Maven

请使用`Maven 3.0.4`及其以上版本。[详情参见](http://maven.apache.org/install.html)

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
