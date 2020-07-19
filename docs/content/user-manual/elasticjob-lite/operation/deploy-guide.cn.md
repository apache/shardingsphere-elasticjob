+++
title = "部署指南"
weight = 1
chapter = true
+++

## 应用部署

1. 启动 ElasticJob-Lite 指定注册中心的 Zookeeper。

2. 运行包含 ElasticJob-Lite 和业务代码的 jar 文件。不限于 jar 或 war 的启动方式。

## 运维平台和 RESTFul API 部署(可选)

1. 解压缩 `elasticjob-lite-console-${version}.tar.gz` 并执行 `bin\start.sh`。

2. 打开浏览器访问 `http://localhost:8899/` 即可访问控制台。8899 为默认端口号，可通过启动脚本输入 `-p` 自定义端口号。

3. 访问 RESTFul API 方法同控制台。

4. `elasticjob-lite-console-${version}.tar.gz`可通过 `mvn install` 编译获取。
