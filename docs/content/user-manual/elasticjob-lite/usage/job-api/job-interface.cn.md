+++
title = "作业开发"
weight = 1
chapter = true
+++

ElasticJob-Lite 和 ElasticJob-Cloud 提供统一作业接口，开发者仅需对业务作业进行一次开发，之后可根据不同的配置以及部署至不同环境。

ElasticJob 的作业分类基于 class 和 type 两种类型。
基于 class 的作业需要开发者自行通过实现接口的方式织入业务逻辑；
基于 type 的作业则无需编码，只需要提供相应配置即可。

基于 class 的作业接口的方法参数 `shardingContext` 包含作业配置、片和运行时信息。
可通过 `getShardingTotalCount()`, `getShardingItem()` 等方法分别获取分片总数，运行在本作业服务器的分片序列号等。

ElasticJob 目前提供 Simple、Dataflow 这两种基于 class 的作业类型，并提供 Script、HTTP 这两种基于 type 的作业类型，用户可通过实现 SPI 接口自行扩展作业类型。

## 简单作业

意为简单实现，未经任何封装的类型。需实现 SimpleJob 接口。
该接口仅提供单一方法用于覆盖，此方法将定时执行。
与Quartz原生接口相似，但提供了弹性扩缩容和分片等功能。

```java
public class MyElasticJob implements SimpleJob {
    
    @Override
    public void execute(ShardingContext context) {
        switch (context.getShardingItem()) {
            case 0: 
                // do something by sharding item 0
                break;
            case 1: 
                // do something by sharding item 1
                break;
            case 2: 
                // do something by sharding item 2
                break;
            // case n: ...
        }
    }
}
```

## 数据流作业

用于处理数据流，需实现 DataflowJob 接口。
该接口提供2个方法可供覆盖，分别用于抓取 (fetchData) 和处理 (processData) 数据。

```java
public class MyElasticJob implements DataflowJob<Foo> {
    
    @Override
    public List<Foo> fetchData(ShardingContext context) {
        switch (context.getShardingItem()) {
            case 0: 
                List<Foo> data = // get data from database by sharding item 0
                return data;
            case 1: 
                List<Foo> data = // get data from database by sharding item 1
                return data;
            case 2: 
                List<Foo> data = // get data from database by sharding item 2
                return data;
            // case n: ...
        }
    }
    
    @Override
    public void processData(ShardingContext shardingContext, List<Foo> data) {
        // process data
        // ...
    }
}
```

***

**流式处理**

可通过属性配置 `streaming.process` 开启或关闭流式处理。

如果开启流式处理，则作业只有在 fetchData 方法的返回值为 null 或集合容量为空时，才停止抓取，否则作业将一直运行下去；
如果关闭流式处理，则作业只会在每次作业执行过程中执行一次 fetchData 和 processData 方法，随即完成本次作业。

如果采用流式作业处理方式，建议 processData 在处理数据后更新其状态，避免 fetchData 再次抓取到，从而使得作业永不停止。

## 脚本作业

支持 shell，python，perl 等所有类型脚本。
可通过属性配置 `script.command.line` 配置待执行脚本，无需编码。
执行脚本路径可包含参数，参数传递完毕后，作业框架会自动追加最后一个参数为作业运行时信息。

例如如下脚本：

```bash
#!/bin/bash
echo sharding execution context is $*
```

作业运行时将输出：

```
sharding execution context is {"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","shardingItem":0,"shardingParameter":"A"}
```

## HTTP作业（3.0.0-beta 提供）

可通过属性配置`http.url`,`http.method`,`http.data`等配置待请求的http信息。
分片信息以Header形式传递，key为`shardingContext`，值为json格式。

```java

public class HttpJobMain {
    
    public static void main(String[] args) {
        
        new ScheduleJobBootstrap(regCenter, "HTTP", JobConfiguration.newBuilder("javaHttpJob", 1)
                .setProperty(HttpJobProperties.URI_KEY, "http://xxx.com/execute")
                .setProperty(HttpJobProperties.METHOD_KEY, "POST")
                .setProperty(HttpJobProperties.DATA_KEY, "source=ejob")
                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing").build()).schedule();
    }
}
```
```java
@Controller
@Slf4j
public class HttpJobController {
    
    @RequestMapping(path = "/execute", method = RequestMethod.POST)
    public void execute(String source, @RequestHeader String shardingContext) {
        log.info("execute from source : {}, shardingContext : {}", source, shardingContext);
    }
}
```

execute接口将输出：
```
execute from source : ejob, shardingContext : {"jobName":"scriptElasticDemoJob","shardingTotalCount":3,"jobParameter":"","shardingItem":0,"shardingParameter":"Beijing"}
```
