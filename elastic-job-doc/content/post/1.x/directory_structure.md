+++
date = "2016-01-27T16:14:21+08:00"
title = "1.x 目录结构说明"
weight=1004
+++

# 目录结构说明

## elastic-job-core

`elastic-job`核心模块，只通过`Quartz`和`Curator`就可执行分布式作业。

## elastic-job-api

`elastic-job`生命周期操作的`API`，可独立使用。

## elastic-job-spring

`elastic-job`对`spring`支持的模块，包括命名空间，依赖注入，占位符等。

## elastic-job-console

`elastic-job web`控制台，可将编译之后的`war`放入`tomcat`等`servlet`容器中使用。

## elastic-job-example

使用示例。

## elastic-job-doc

使用`markdown`生成文档的项目，使用方无需关注。
