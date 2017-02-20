+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Cloud部署指南"
weight=53
+++

# Elastic-Job-Cloud部署指南

## 部署步骤

1. 启动`Zookeeper`, `Mesos Master/Agent`以及`Elastic-Job-Cloud-Scheduler`。

2. 将打包之后的作业`tar.gz`文件放至网络可访问的位置，如：`ftp`或`http`。打包的`tar.gz`文件中`Main`方法需要调用`Elastic-Job-Cloud`提供的`JobBootstrap.execute`方法。

3. 使用`curl`命令调用`REST API`注册APP及作业，详情参见：[Elastic-Job-Cloud RESTful API](http://dangdangdotcom.github.io/elastic-job/post/cloud_restful_api)。
