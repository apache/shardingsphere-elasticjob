+++
title = "运维平台"
weight = 4
chapter = true
+++

解压缩 `elasticjob-lite-console-${version}.tar.gz` 并执行 `bin\start.sh`。
打开浏览器访问 `http://localhost:8899/` 即可访问控制台。
8899 为默认端口号，可通过启动脚本输入 `-p` 自定义端口号。

## 登录

控制台提供两种账户：管理员及访客。
管理员拥有全部操作权限，访客仅拥有察看权限。
默认管理员用户名和密码是 root/root，访客用户名和密码是 guest/guest，可通过 `conf\application.properties` 修改管理员及访客用户名及密码。
```
auth.root_username=root
auth.root_password=root
auth.guest_username=guest
auth.guest_password=guest
```

## 功能列表

- 登录安全控制
- 注册中心、事件追踪数据源管理
- 快捷修改作业设置
- 作业和服务器维度状态查看
- 操作作业禁用\启用、停止和删除等生命周期
- 事件追踪查询

## 设计理念

运维平台和 ElasticJob-Lite 并无直接关系，是通过读取作业注册中心数据展现作业状态，或更新注册中心数据修改全局配置。

控制台只能控制作业本身是否运行，但不能控制作业进程的启动，因为控制台和作业本身服务器是完全分离的，控制台并不能控制作业服务器。

## 不支持项

* 添加作业

作业在首次运行时将自动添加。
ElasticJob-Lite 以 jar 方式启动，并无作业分发功能。
如需完全通过运维平台发布作业，请使用 ElasticJob-Cloud。
