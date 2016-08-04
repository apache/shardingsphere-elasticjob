+++
date = "2016-01-27T16:14:21+08:00"
title = "1.x 作业分片策略"
weight=1011
+++

# 作业分片策略

## 框架提供的分片策略

### AverageAllocationJobShardingStrategy

**全路径：**

`com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy`

**策略说明：**

基于平均分配算法的分片策略，也是默认的分片策略。

如果分片不能整除，则不能整除的多余分片将依次追加到序号小的服务器。如：

如果有`3`台服务器，分成`9`片，则每台服务器分到的分片是：`1=[0,1,2], 2=[3,4,5], 3=[6,7,8]`

如果有`3`台服务器，分成`8`片，则每台服务器分到的分片是：`1=[0,1,6], 2=[2,3,7], 3=[4,5]`

如果有`3`台服务器，分成`10`片，则每台服务器分到的分片是：`1=[0,1,2,9], 2=[3,4,5], 3=[6,7,8]`

### OdevitySortByNameJobShardingStrategy


**全路径：**

`com.dangdang.ddframe.job.plugin.sharding.strategy.OdevitySortByNameJobShardingStrategy`

**策略说明：**

根据作业名的哈希值奇偶数决定`IP`升降序算法的分片策略。

作业名的哈希值为奇数则`IP`升序。

作业名的哈希值为偶数则`IP`降序。

用于不同的作业平均分配负载至不同的服务器。

`AverageAllocationJobShardingStrategy`的缺点是，一旦分片数小于作业服务器数，作业将永远分配至`IP`地址靠前的服务器，导致`IP`地址靠后的服务器空闲。而`OdevitySortByNameJobShardingStrategy`则可以根据作业名称重新分配服务器负载。如：

如果有`3`台服务器，分成`2`片，作业名称的哈希值为奇数，则每台服务器分到的分片是：`1=[0], 2=[1], 3=[]`

如果有`3`台服务器，分成`2`片，作业名称的哈希值为偶数，则每台服务器分到的分片是：`3=[0], 2=[1], 1=[]`

### RotateServerByNameJobShardingStrategy

**全路径：**

`com.dangdang.ddframe.job.plugin.sharding.strategy.RotateServerByNameJobShardingStrategy`

**策略说明：**

根据作业名的哈希值对服务器列表进行轮转的分片策略。

## 自定义分片策略

实现`JobShardingStrategy`接口并实现`sharding`方法，接口方法参数为作业服务器`IP`列表和分片策略选项，分片策略选项包括作业名称，分片总数以及分片序列号和个性化参数对照表，可以根据需求定制化自己的分片策略。

欢迎将分片策略以插件的形式贡献至`com.dangdang.ddframe.job.plugin.sharding.strategy`包。

## 配置分片策略

与配置通常的作业属性相同，在`spring`命名空间或者`JobConfiguration`中配置`jobShardingStrategyClass`属性，属性值是作业分片策略类的全路径。

