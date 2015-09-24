##Elastic-Job - distributed scheduled job solution

  Elastic-Job is extracted from dd-job that is a component for ddframe. Elastic-Job just remove monitor and integrate standard parts in dd-job. It based on matured open source productions like Quartz, Zookeeper and its client Curator.
  
  Other components for ddframe also can open source independently. Dangdang has already open source DubboX, which is core component for dd-soa.
  
  The relationship for Elastic-Job and ddframe show in this picture:
  
  ![Evolution chart for ddframe](http://static.oschina.net/uploads/space/2015/0915/181703_2fxp_719192.jpg)

##Contributors
* Zhang Liang [dangadng](http://www.dangdang.com/) zhangliang@dangdang.com
* Cao Hao [dangadng](http://www.dangdang.com/) caohao@dangdang.com
* Jiang Shu Jian [dangadng](http://www.dangdang.com/) jiangshujian@dangdang.com

## Features

* **Scheduled job: ** Based on CRON expression to execute tasks.
* **Registry center: ** Based on Zookeeper and its client Curator to implement global job register center, use to register, monitor control and coordinate distributed jobs.
* **Sharding: ** Split one task to many task items, can execute in multiple servers.
* **scalability: ** Crashed some running job servers or add on some new job servers, elastic-job will rehsarding at next job trigger, will not affect current jobs.
* **Various job models: ** Now support OneOff, Perpetual and SequencePerpetual 3 type job models.
* **Failover: ** Crashed some running job servers will not cause job re-sharding, will do it at next job trigger. Enable failover can use other idle servers to pull orphan task items to execute during current job execution life-cycle.
* **Runtime status collector: ** Monitor runtime status for jobs, statistics count for process success and failure, record previous trigger time, completed time and next trigger time.
* **Job pause, resume and disable: **Can pause and resume jobs, and can disable job server(usually setting during system launch).
* **Misfired job re-trigger: **Record missing jobs automatically, and trigger them after previous job completed. Please reference Quartz misfire.
* **Data processed concurrently: **Use concurrent threads processing fetched data, to improve throughput.
* **Idempotency: **Judge duplicate jobs, restrict running jobs execute twice. Because enable idempotency need monitor job running status, the performance for instantaneous jobs maybe low.
* **Failure tolerance: **If job servers loss connection from zookeeper, job will stop immediately which to prevent register center assign crashed task items to other job servers, but current job servers still run jobs, to cause duplicated.
* **Spring support: **Spring framework integrated, customized namespace, place-holder supported etc.
* **Web console: **Support web console, use to manage jobs and register centers.

## Related documents

[Directory structure](http://dangdangdotcom.github.io/elastic-job/directoryStructure_en.html)

[Usage](http://dangdangdotcom.github.io/elastic-job/usage_en.html)

[User guide](http://dangdangdotcom.github.io/elastic-job/userGuide_en.html)

[Limitations](http://dangdangdotcom.github.io/elastic-job/limitations_en.html)

[Web console](http://dangdangdotcom.github.io/elastic-job/webConsole_en.html)

[Source codes compile problems](http://dangdangdotcom.github.io/elastic-job/sourceCodeGuide_en.html)

[Theory illustrate](http://dangdangdotcom.github.io/elastic-job/theory_en.html)

[InfoQ news](http://www.infoq.com/cn/news/2015/09/dangdang-elastic-job)

## Quick Start

* **Add maven dependencies**

elastic-job has already into Maven Central Repository, add dependencies in your pom.xml file.

```xml
<!-- add elastic-job core module -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-core</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- add elastic-job spring module, use namespace -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-spring</artifactId>
    <version>1.0.1</version>
</dependency>
```
* **Job development**

```java
public class MyElasticJob extends AbstractPerpetualElasticJob<Foo> {
    
    @Override
    protected List<Foo> fetchData(JobExecutionMultipleShardingContext context) {
        List<Foo> result = // get data from database by sharding items
        return result;
    }
    
    @Override
    protected boolean processData(JobExecutionMultipleShardingContext context, Foo data) {
        // process data
        return true;
    }
}
```

* **Job configuration**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:reg="http://www.dangdang.com/schema/ddframe/reg" 
    xmlns:job="http://www.dangdang.com/schema/ddframe/job" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.dangdang.com/schema/ddframe/reg 
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd 
                        http://www.dangdang.com/schema/ddframe/job 
                        http://www.dangdang.com/schema/ddframe/job/job.xsd 
                        ">
    <!--configure registry center -->
    <reg:zookeeper id="regCenter" serverLists=" yourhost:2181" namespace="dd-job" baseSleepTimeMilliseconds="1000" maxSleepTimeMilliseconds="3000" maxRetries="3" />
    
    <!--configure job -->
    <job:bean id="oneOffElasticJob" class="xxx.MyElasticJob" regCenter="regCenter" cron="0/10 * * * * ?"   shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C" />
</beans>
```
