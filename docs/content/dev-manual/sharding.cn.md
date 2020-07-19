+++
pre = "<b>5.1. </b>"
title = "作业分片策略"
weight = 1
chapter = true
+++

作业分片策略，用于将作业在分布式环境下分解成为任务使用。

| *SPI 名称*                             | *详细说明*                                                     |
| ------------------------------------- | ------------------------------------------------------------- |
| JobShardingStrategy                   | 作业分片策略                                                    |

| *已知实现类*                           | *详细说明*                                                      |
| ------------------------------------- | -------------------------------------------------------------- |
| AverageAllocationJobShardingStrategy  | 根据分片项平均分片                                               |
| OdevitySortByNameJobShardingStrategy  | 根据作业名称哈希值的奇偶数决定按照作业服务器 IP 升序或是降序的方式分片 |
| RotateServerByNameJobShardingStrategy | 根据作业名称轮询分片                                             |
