+++
toc = true
date = "2016-09-27T16:14:21+08:00"
title = "定制化处理"
weight = 31
prev = "/02-guide/operation-manual/"
next = "/03-design"
+++

Elastic-Job在配置中提供了JobProperties，用于定制化处理，目前支持自定义异常处理及作业处理线程池。

## 异常处理

可扩展JobExceptionHandler接口，并设置job_exception_handler定制异常处理流程，默认实现是记录日志但不抛出异常。

## 作业处理线程池

可扩展ExecutorServiceHandler接口，并设置executor_service_handler定制线程池。
