+++
pre = "<b>2.2. </b>"
title = "ElasticJob-Cloud"
weight = 2
chapter = true
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-cloud-executor</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## Develop Job Details

```java
public class MyJob implements SimpleJob {
    
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

## Develop Job Bootstrap

Define `main` method and call `JobBootstrap.execute()`, example as follows:

```java
public class MyJobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute(new MyJob());
    }
}
```

## Pack Job

```bash
tar -cvf my-job.tar.gz my-job
```

## API Authentication

```bash
curl -H "Content-Type: application/json" -X POST http://elasticjob_cloud_host:8899/api/login -d '{"username": "root", "password": "pwd"}'
```

Response body:
```json
{"accessToken":"some_token"}
```


## Publish Job

```bash
curl -l -H "Content-type: application/json" -H "accessToken: some_token" -X POST -d '{"appName":"my_app","appURL":"http://app_host:8080/my-job.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true,"eventTraceSamplingCount":0}' http://elasticjob_cloud_host:8899/api/app
```

## Schedule Job

```bash
curl -l -H "Content-type: application/json" -H "accessToken: some_token" -X POST -d '{"jobName":"my_job","appName":"my_app","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0}' http://elasticjob_cloud_host:8899/api/job/register
```
