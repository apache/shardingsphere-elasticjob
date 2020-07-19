+++
pre = "<b>5.3. </b>"
title = "定制化处理"
weight = 3
chapter = true
+++

ElasticJob 在配置中提供了 JobHandler，用于定制化处理，目前支持自定义异常处理及作业处理线程池。

## 异常处理

可扩展 JobErrorHandler 接口，默认实现是记录日志但不抛出异常。

## 作业处理线程池

可扩展 JobExecutorServiceHandler 接口，定制线程池。
