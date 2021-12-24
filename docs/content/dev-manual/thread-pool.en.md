+++
pre = "<b>5.2. </b>"
title = "Thread Pool Strategy"
weight = 2
+++

Thread pool strategy, used to create thread pool for job execution. 

| *SPI Name*                            | *Description*                                          |
| ------------------------------------- | ------------------------------------------------------ |
| JobExecutorServiceHandler             | Job executor service handler                           |

| *Implementation Class*                | *Description*                                          |
| ------------------------------------- | ------------------------------------------------------ |
| CPUUsageJobExecutorServiceHandler     | Use CPU available processors * 2 to create thread pool |
| SingleThreadJobExecutorServiceHandler | Use single thread to execute job                       |
