
+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Lite部署指南"
weight=13
+++

# Elastic-Job-Lite部署指南

## Elastic-Job-Lite

1. 启动`Elastic-Job-Lite`指定注册中心的`Zookeeper`。

2. 部署运维平台`war`文件至任何支持`Servlet`的`Web`容器(可选)。
运维平台以`war`包形式提供，可自行部署至`tomcat`或`jetty`等支持`servlet`的`web`容器中。`elastic-job-console.war`可通过`mvn install`编译或`maven`中央仓库获取。

3. 运行包含`Elastic-Job-Lite`和业务代码的`jar`文件。
