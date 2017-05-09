+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "部署指南"
weight = 13
prev = "/01-start/dev-guide/"
next = "/02-guide"
+++

## 应用部署

1. 启动Elastic-Job-Lite指定注册中心的Zookeeper。

2. 运行包含Elastic-Job-Lite和业务代码的jar文件。不限与jar或war的启动方式。

## 运维平台和RESTFul API部署(可选)

1. 解压缩elastic-job-lite-console-${version}.tar.gz并执行bin\start.sh。

2. 打开浏览器访问`http://localhost:8899/`即可访问控制台。8899为默认端口号，可通过启动脚本输入-p自定义端口号。

3. 访问RESTFul API方法同控制台。

4. elastic-job-lite-console-${version}.tar.gz可通过mvn install编译获取。
