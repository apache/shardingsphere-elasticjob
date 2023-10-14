+++
title = "运维平台"
weight = 4
chapter = true
+++

解压缩 `elasticjob-console-${version}.tar.gz` 并执行 `bin\start.sh`。
打开浏览器访问 `http://localhost:8088/` 即可访问控制台。
8088 为默认端口号，可通过启动脚本输入 `-p` 自定义端口号。

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

## Casdoor 登录

控制台集成了[Casdoor](https://casdoor.org/)单点登录,用户可以选择 Casdoor 进行登录等一些列操作。

步骤一: 部署 casdoor
Casdoor的源代码托管在 GitHub: https://github.com/casdoor/casdoor 

启动模式有开发模式和生产模式,此处以开发模式为例,[更多详细](https://casdoor.org/docs/basic/server-installation)

后端启动方式

```bash
go run main.go
```

前端启动方式

```bash
cd web
yarn install
yarn start
```

步骤二:配置casdoor并得到所需的数据

![casdoorConfig](https://shardingsphere.apache.org/elasticjob/current/img/casdoor/casdoorConfig.png)

用红线指出来的是后端配置需要用到的,其中Redirect URLs取决于你要要callback的地址

我们还需要根据所选cert到cert选项中找到对应的cert,如本例为cert-built-in,其中certificate也是我们所需要用到的。

![cert](https://shardingsphere.apache.org/elasticjob/current/img/casdoor/cert.png)

更多[casdoor文档](https://casdoor.org/docs/overview)

步骤三:在ShardingSphere中进行配置

在[sharingshphere-elasticjob-ui](https://github.com/apache/shardingsphere-elasticjob-ui)中的该application.properties进行配置

![list](https://shardingsphere.apache.org/elasticjob/current/img/casdoor/list.png)

将我们在casdoor获取的数据粘贴到相应位置即可如:

![application](https://shardingsphere.apache.org/elasticjob/current/img/casdoor/application.png)

这样我们就可以在shardingsphere-elasticjob-ui中使用casdoor了！更多功能详见[Casdoor](https://casdoor.org/)

## 功能列表

- 登录安全控制
- 注册中心、事件追踪数据源管理
- 快捷修改作业设置
- 作业和服务器维度状态查看
- 操作作业禁用\启用、停止和删除等生命周期
- 事件追踪查询

## 设计理念

运维平台和 ElasticJob 并无直接关系，是通过读取作业注册中心数据展现作业状态，或更新注册中心数据修改全局配置。

控制台只能控制作业本身是否运行，但不能控制作业进程的启动，因为控制台和作业本身服务器是完全分离的，控制台并不能控制作业服务器。

## 不支持项

* 添加作业

作业在首次运行时将自动添加。
ElasticJob 以 jar 方式启动，并无作业分发功能。
