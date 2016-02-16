##Elastic-Job - distributed scheduled job solution
**License:** [![Hex.pm](http://dangdangdotcom.github.io/elastic-job/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

**Maven Central:** [![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
  Elastic-Job is extracted from dd-job which is a component of ddframe. Elastic-Job just removed monitor and integrated standards parts from dd-job. It based on matured open-source productions like Quartz, Zookeeper and its client Curator.
  
  Other components of ddframe also can open-source independently. Dangdang has already released DubboX, which is core component of dd-soa.
  
  The relationship between Elastic-Job and ddframe is in this picture:
  
  ![Evolution diagram of ddframe](http://dangdangdotcom.github.io/elastic-job/img/ddframe.jpg)

##Contributors
* Zhang Liang [Dangdang](http://www.dangdang.com/) zhangliang@dangdang.com
* Cao Hao [Dangdang](http://www.dangdang.com/) caohao@dangdang.com
* Jiang Shu Jian [Dangdang](http://www.dangdang.com/) jiangshujian@dangdang.com

## Features

* **Scheduled job:** Based on CRON expression to execute jobs.
* **Registry center:** Based on Zookeeper and its client Curator to implement global job register center, use to register, monitor control and coordinate distributed jobs.
* **Sharding:** Split single task to many task items, execute parallel on multiple servers.
* **Scalability:** Server crashed or new server online, elastic-job will re-sharding when next job trigger, will not affect current running jobs.
* **Multiple job modes:** Now support Simple, ThroughputDataFlow and SequenceDataFlow job modes.
* **Failover:** Server crashed does not trigger re-sharding, only do it when next task trigger. Enable failover can notify other idle servers to pull orphan task items.
* **Execution status collection:** Monitor execution status and statistics process success and failure count, collect previous trigger time, completed time and next trigger time.
* **Pause, resume and disable:** Pause or resume jobs, and disable servers (usually disabled during system launching).
* **Misfired job re-trigger:** Record missing jobs automatically, and trigger them after previous task completed. Please reference Quartz misfire.
* **Data processed concurrently:** Use concurrent threads processing fetched data, to improve throughput.
* **Idempotency:** Judge duplicate task items, restrict repeatable task items execute. Because enable idempotency need monitor job execution status, the performance for instantaneous jobs maybe low.
* **Failure tolerance:** If job servers lost connection from registry center, job will stop immediately which to prevent registry center assign crashed task items to other job servers, but current job servers still running, then cause duplicated task items running.
* **Data offset store:**Store offset of last procesed data into Zookeeper.
* **Spring support:** Integrate spring framework, customized namespace, place-holder supported etc.
* **Web console:** Support web console to manage jobs and register centers.

## Related documents

[Downloads](http://dangdangdotcom.github.io/elastic-job/downloads_en.html)

[Release notes](http://dangdangdotcom.github.io/elastic-job/releaseNotes_en.html)

[Directory structure](http://dangdangdotcom.github.io/elastic-job/directoryStructure_en.html)

[Usage](http://dangdangdotcom.github.io/elastic-job/usage_en.html)

[User guide](http://dangdangdotcom.github.io/elastic-job/userGuide_en.html)

[Limitations](http://dangdangdotcom.github.io/elastic-job/limitations_en.html)

[Web console](http://dangdangdotcom.github.io/elastic-job/webConsole_en.html)

[Source codes compile problems](http://dangdangdotcom.github.io/elastic-job/sourceCodeGuide_en.html)

[Theory illustrate](http://dangdangdotcom.github.io/elastic-job/theory_en.html)

[Job sharding strategy](http://dangdangdotcom.github.io/elastic-job/jobStrategy_en.html)

[InfoQ news](http://www.infoq.com/cn/news/2015/09/dangdang-elastic-job)

## Quick Start

* **Add maven dependencies**

Elastic-Job has deployed to Maven Central Repository, add dependencies in your pom.xml file.

```xml
<!-- add elastic-job core module -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-core</artifactId>
    <version>1.0.3</version>
</dependency>

<!-- add elastic-job spring module, use namespace -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-spring</artifactId>
    <version>1.0.3</version>
</dependency>
```
* **Job development**

```java
public class MyElasticJob extends AbstractThroughputDataFlowElasticJob<Foo> {
    
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
