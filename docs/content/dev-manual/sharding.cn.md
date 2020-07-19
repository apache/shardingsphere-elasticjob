+++
pre = "<b>5.1. </b>"
title = "作业分片策略"
weight = 1
chapter = true
+++

| *SPI 名称*                             | *详细说明*                                     |
| ------------------------------------- | ---------------------------------------------- |
| JobShardingStrategy                   | 作业分片策略                                    |

| *已知实现类*                           | *详细说明*                                      |
| ------------------------------------- | ---------------------------------------------- |
| AverageAllocationJobShardingStrategy  | 基于平均分配算法的分片策略                        |
| OdevitySortByNameJobShardingStrategy  | 根据作业名的哈希值奇偶数决定 IP 升降序算法的分片策略 |
| RotateServerByNameJobShardingStrategy | 根据作业名的哈希值对服务器列表进行轮转的分片策略     |
