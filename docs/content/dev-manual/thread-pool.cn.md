+++
pre = "<b>5.2. </b>"
title = "线程池策略"
weight = 2
+++

线程池策略，用于执行作业的线程池创建。

| *SPI 名称*                          | *详细说明*       |
|-----------------------------------|--------------|
| JobExecutorThreadPoolSizeProvider | 作业执行线程数量提供策略 |

| *已知实现类*                                       | *详细说明*                  |
|-----------------------------------------------|-------------------------|
| CPUUsageJobExecutorThreadPoolSizeProvider     | 根据 CPU 核数 * 2 创建作业处理线程池 |
| SingleThreadJobExecutorThreadPoolSizeProvider | 使用单线程处理作业               |
