+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "运维平台"
weight = 11
prev = "/02-guide"
next = "/02-guide/job-sharding-strategy/"
+++

Elastic-Job运维平台以war包形式提供，可自行部署到tomcat或jetty等支持servlet的web容器中。

elastic-job-console.war可以通过编译源码或从maven中央仓库获取。

## 登录

默认用户名和密码是root/root，可以通过修改conf\auth.properties文件修改默认登录用户名和密码。

## 主要功能

* 登录安全控制

* 注册中心管理

* 作业维度状态查看

* 服务器维度状态查看

* 快捷修改作业设置

* 控制作业暂停，恢复运行，停止和删除

## 设计理念

运维平台和elastic-job并无直接关系，是通过读取作业注册中心数据展现作业状态，或更新注册中心数据修改全局配置。

控制台只能控制作业本身是否运行，但不能控制作业进程的启停，因为控制台和作业本身服务器是完全分布式的，控制台并不能控制作业服务器。

## 不支持项

* 添加作业。因为作业都是在首次运行时自动添加，使用运维平台添加作业并无必要。

## 主要界面

* 总览页

![总览页](/img/1.x/console_index.png)

* 注册中心管理页

![注册中心管理页](/img/1.x/console_reg_center.png)

* 作业详细信息页

![作业详细信息页](/img/1.x/console_job_details.png)

* 服务器详细信息页

![服务器详细信息页](/img/1.x/console_server_details.png)