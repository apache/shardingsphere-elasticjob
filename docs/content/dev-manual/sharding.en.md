+++
pre = "<b>5.1. </b>"
title = "Job Sharding Strategy"
weight = 1
chapter = true
+++

Job Sharding Strategy， used to sharding job to distributed tasks.

| *SPI Name*                            | *Description*                                               |
| ------------------------------------- | ----------------------------------------------------------- |
| JobShardingStrategy                   | Job sharding strategy                                       |

| *Implementation Class*                | *Description*                                               |
| ------------------------------------- | ----------------------------------------------------------- |
| AverageAllocationJobShardingStrategy  | Sharding or average by sharding item                        |
| OdevitySortByNameJobShardingStrategy  | Sharding for hash with job name to determine IP asc or desc |
| RotateServerByNameJobShardingStrategy | Sharding for round robin by name job                        |
