+++
date = "2016-09-27T16:14:21+08:00"
title = "Elastic-Job定制化处理"
weight=31
+++

# 异常处理

`Elastic-Job`在配置中提供了`JobProperties`，可扩展`JobExceptionHandler`接口，并设置`job_exception_handler`定制异常处理流程，默认实现是记录日志但不抛出异常。

# 定制化作业处理线程池

`Elastic-Job`在配置中提供了`JobProperties`，可扩展`ExecutorServiceHandler`接口，并设置`executor_service_handler`定制线程池。
