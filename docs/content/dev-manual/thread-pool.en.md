+++
pre = "<b>5.2. </b>"
title = "Thread Pool Strategy"
weight = 2
+++

Thread pool strategy, used to create thread pool for job execution. 

| *SPI Name*                        | *Description*                          |
|-----------------------------------|----------------------------------------|
| JobExecutorThreadPoolSizeProvider | Job executor thread pool size provider |

| *Implementation Class*                        | *Description*                                          |
|-----------------------------------------------|--------------------------------------------------------|
| CPUUsageJobExecutorThreadPoolSizeProvider     | Use CPU available processors * 2 to create thread pool |
| SingleThreadJobExecutorThreadPoolSizeProvider | Use single thread to execute job                       |
