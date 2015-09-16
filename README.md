##Elastic-Job - distributed scheduled job solution

  Elastic-Job是ddframe中dd-job的作业模块中分离出来的分布式弹性作业框架。去掉了和dd-job中的监控和ddframe接入规范部分。

  ddframe其他模块也有可独立开源的部分，之前当当曾开源过dd-soa的基石模块DubboX。
  
  elastic-job和ddframe关系见下图
  
  ![ddframe演进图](http://static.oschina.net/uploads/space/2015/0915/181703_2fxp_719192.jpg)

##主要贡献者
* 张亮 [当当网](http://www.dangdang.com/) zhangliang@dangdang.com
* 曹昊 [当当网](http://www.dangdang.com/) caohao@dangdang.com
* 江树建 [当当网](http://www.dangdang.com/) jiangshujian@dangdang.com

## Elastic-Job主要功能

* **定时任务：** 基于成熟的定时任务作业框架Quartz cron表达式执行定时任务。
* **作业注册中心：** 基于Zookeeper和其客户端Curator实现的全局作业注册控制中心。用于注册，控制和协调分布式作业执行。
* **作业分片：** 将一个任务分片成为多个小任务项在多服务器上同时执行。
* **弹性扩容缩容：** 运行中的作业服务器崩溃，或新增加n台作业服务器，作业框架将在下次作业执行前重新分片，不影响当前作业执行。
* **支持多种作业执行模式：** 支持OneOff，Perpetual和SequencePerpetual三种作业模式。
* **失效转移：** 运行中的作业服务器崩溃不会导致重新分片，只会在下次作业启动时分片。启用失效转移功能可以在本次作业执行过程中，监测其他作业服务器空闲，抓取未完成的孤儿分片项执行。
* **运行时状态收集：** 监控作业运行时状态，统计最近一段时间处理的数据成功和失败数量，记录作业上次运行开始时间，结束时间和下次运行时间。
* **作业停止，恢复和禁用：**用于操作作业启停，并可以禁止某作业运行（上线时常用）。
* **被错过执行的作业重触发：**自动记录错过执行的作业，并在上次作业完成后自动触发。可参考Quartz的misfire。
* **多线程快速处理数据：**使用多线程处理抓取到的数据，提升吞吐量。
* **幂等性：**重复作业任务项判定，不重复执行已运行的作业任务项。由于开启幂等性需要监听作业运行状态，对瞬时反复运行的作业对性能有较大影响。
* **容错处理：**作业服务器与Zookeeper服务器通信失败则立即停止作业运行，防止作业注册中心将失效的分片分项配给其他作业服务器，而当前作业服务器仍在执行任务，导致重复执行。
* **Spring支持：**支持spring容器，自定义命名空间，支持占位符。
* **运维平台：**提供运维界面，可以管理作业和注册中心。

## Quick Start

* **目录结构说明**
 
  **elastic-job-platform：**maven版本依赖模块，编译elastic-job主项目前需先编译本项目。
  
  **elastic-job-core：**elastic-job核心模块，只通过Quartz和Curator就可执行分布式作业。
  
  **elastic-job-spring：**elastic-job对spring支持的模块，包括命名空间，依赖注入，占位符等。
  
  **elastic-job-console：**elastic-job web控制台，可将编译之后的war放入tomcat等servlet容器中使用。
  
  **elastic-job-example：**使用例子。

  **elastic-job-test：**测试elastic-job使用的公用类，使用方无需关注。

* **使用步骤**

  **安装Java环境**
  
  请使用JDK1.7及其以上版本。可参考http://www.oracle.com/technetwork/java/javase/downloads/index.html

  **安装Zookeeper**
  
  请使用Zookeeper 3.4.6及其以上版本。可参考https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html
  
  **安装Maven**
  
  请使用Maven 3.0.4及其以上版本。可参考http://maven.apache.org/install.html
  
  **编译elastic-job-platform和elastic-job**
```
  cd %elastic-job-source-folder%/elastic-job-platform
  mvn clean install
  cd %elastic-job-source-folder%
  mvn clean install
```

## 代码开发 

  提供3种作业类型，分别是OneOff, Perpetual和SequencePerpetual。需要继承相应的抽象类。
  
  方法参数shardingContext包含作业配置，分片和运行时信息。可通过getShardingTotalCount(),getShardingItems()等方法分别获取分片总数，运行在本作业服务器的分片序列号集合等。

* **OneOff类型作业**

  OneOff作业类型比较简单，需要继承AbstractOneOffElasticJob，该类只提供了一个方法用于覆盖，此方法将被定时执行。用于执行普通的定时任务，与Quartz原生接口相似，只是增加了弹性扩缩容和分片等功能。

```java
public class MyElasticJob extends AbstractOneOffElasticJob {
    
    @Override
    protected void process(JobExecutionMultipleShardingContext context) {
        // do something by sharding items
    }
}
```

* **Perpetual类型作业**

  Perpetual作业类型略为复杂，需要继承AbstractPerpetualElasticJob并可以指定返回值泛型，该类提供两个方法可覆盖，分别用于抓取和处理数据。可以获取数据处理成功失败次数等辅助监控信息。**需要注意fetchData方法的返回值只有为null或长度为空时，作业才会停止执行，否则作业会一直运行下去。**这点是参照TbSchedule的设计。Perpetual作业类型更适用于流式不间歇的数据处理。

  作业执行时会将fetchData的数据传递给processData处理，其中processData得到的数据是通过多线程（线程池大小可配）拆分的。建议processData处理数据后，更新其状态，避免fetchData再次抓取到，从而使得作业永远不会停止。processData的返回值用于表示数据是否处理成功，抛出异常或者返回false将会在统计信息中归入失败次数，返回true则归入成功次数。

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

* **SequencePerpetual类型作业**

  SequencePerpetual作业类型和Perpetual作业类型极为相似，所不同的是Perpetual作业类型可以将获取到的数据多线程处理，但不会保证多线程处理数据的顺序。如：从2个分片共获取到100条数据，第1个分片40条，第2个分片60条，配置为两个线程处理，则第1个线程处理前50条数据，第2个线程处理后50条数据，无视分片项；SequencePerpetual类型作业则根据当前服务器所分配的分片项数量进行多线程处理，每个分片项使用同一线程处理，防止了同一分片的数据被多线程处理，从而导致的顺序问题。如：从2个分片共获取到100条数据，第1个分片40条，第2个分片60条，则系统自动分配两个线程处理，第1个线程处理第1个分片的40条数据，第2个线程处理第2个分片的60条数据。由于Perpetual作业可以使用多余分片项的任意线程数处理，所以性能调优的可能会优于SequencePerpetual作业。

```java
public class MyElasticJob extends AbstractSequencePerpetualElasticJob<Foo> {
    
    @Override
    protected List<Foo> fetchData(JobExecutionSingleShardingContext context) {
        List<Foo> result = // get data from database by sharding items
        return result;
    }
    
    @Override
    protected boolean processData(JobExecutionSingleShardingContext context, Foo data) {
        // process data
        return true;
    }
}
```

## 作业配置

  与Spring容器配合使用作业，可以将作业Bean配置为Spring Bean， 可在作业中通过依赖注入使用Spring容器管理的数据源等对象。可用placeholder占位符从属性文件中取值。

* **Spring命名空间配置**

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
    <!--配置作业注册中心 -->
    <reg:zookeeper id="regCenter" serverLists=" yourhost:2181" namespace="dd-job" baseSleepTimeMilliseconds="1000" maxSleepTimeMilliseconds="3000" maxRetries="3" />
    
    <!-- 配置作业A-->
    <job:bean id="oneOffElasticJob" class="xxx.MyOneOffElasticJob" regCenter="regCenter" cron="0/10 * * * * ?"   shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C" />
    
    <!-- 配置作业B-->
    <job:bean id="perpetualElasticJob" class="xxx.MyPerpetualElasticJob" regCenter="regCenter" cron="0/10 * * * * ?" shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C" processCountIntervalSeconds="10" concurrentDataProcessThreadCount="10" />
</beans>
```

  **job:bean命名空间属性详细说明**

<table>
<tbody>
<tr><td><em>属性名</em></td><td><em>类型</em></td><td><em>是否必填</em></td><td><em>缺省值</em></td><td><em>描述</em></td></tr>
<tr><td>id</td><td>String</td><td>是</td><td></td><td>作业名称</td></tr>
<tr><td>class</td><td>String</td><td>是</td><td></td><td>作业实现类，需实现ElasticJob接口</td></tr>
<tr><td>regCenter</td><td>String</td><td>是</td><td></td><td>注册中心Bean的引用，需引用reg:zookeeper的声明</td></tr>
<tr><td>cron</td><td>String</td><td>是</td><td></td><td>cron表达式，用于配置作业触发时间</td></tr>
<tr><td>shardingTotalCount</td><td>int</td><td>是</td><td></td><td>作业分片总数</td></tr>
<tr><td>shardingItemParameters</td><td>String</td><td>否</td><td></td><td>分片序列号和个性化参数对照表<br>分片序列号和参数用等号分隔，多个键值对用逗号分隔<br>分片序列号从0开始，不可大于或等于作业分片总数<br>如：<br>0=a,1=b,2=c</td></tr>
<tr><td>jobParameter</td><td>String</td><td>否</td><td></td><td>作业自定义参数<br>可以配置多个相同的作业，但是用不同的参数作为不同的调度实例</td></tr>
<tr><td>monitorRuntime</td><td>boolean</td><td>否</td><td>true</td><td>监控作业运行时状态<br>每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br>每次作业执行时间和间隔时间均较长短的情况，建议监控作业运行时状态，可保证数据不会重复选取。
</td></tr>
<tr><td>processCountIntervalSeconds</td><td>int</td><td>否</td><td>300</td><td>统计作业处理数据数量的间隔时间<br>单位：秒<br>对Perpetual和SequencePerpetual作业有效</td></tr>
<tr><td>concurrentDataProcessThreadCount</td><td>int</td><td>否</td><td>1</td><td>同时处理数据的并发线程数<br>不能小于1<br>仅Perpetual作业有效</td></tr>
<tr><td>fetchDataCount</td><td>int</td><td>否</td><td>0</td><td>每次抓取的数据量</td></tr>
<tr><td>failover</td><td>boolean</td><td>否</td><td>false</td><td>是否开启失效转移<br>仅monitorExecution开启，失效转移才有效</td></tr>
<tr><td>description</td><td>String</td><td>否</td><td></td><td>作业描述信息</td></tr>
<tr><td>disabled</td><td>boolean</td><td>否</td><td>false</td><td>作业是否禁止启动<br>可用于部署作业时，先禁止启动，部署结束后统一启动</td></tr>
<tr><td>overwrite</td><td>boolean</td><td>否</td><td>false</td><td>本地配置是否可覆盖注册中心配置<br>如果可覆盖，每次启动作业都以本地配置为准</td></tr>
</tbody>
</table>

  **reg:zookeeper命名空间属性详细说明**

<table>
<tbody>
<tr><td><em>属性名</em></td><td><em>类型</em></td><td><em>是否必填</em></td><td><em>缺省值</em></td><td><em>描述</em></td></tr>
<tr><td>id</td><td>String</td><td>是</td><td></td><td>注册中心在Spring容器中的主键</td></tr>
<tr><td>serverLists</td><td>String</td><td>是</td><td></td><td>连接Zookeeper服务器的列表<br>包括IP地址和端口号<br>多个地址用逗号分隔<br>如: host1:2181,host2:2181</td></tr>
<tr><td>namespace</td><td>String</td><td>是</td><td></td><td>Zookeeper的命名空间</td></tr>
<tr><td>baseSleepTimeMilliseconds</td><td>int</td><td>是</td><td></td><td>等待重试的间隔时间的初始值<br>单位：毫秒</td></tr>
<tr><td>maxSleepTimeMilliseconds</td><td>int</td><td>是</td><td></td><td>等待重试的间隔时间的最大值<br>单位：毫秒</td></tr>
<tr><td>maxRetries</td><td>int</td><td>是</td><td></td><td>最大重试次数</td></tr>
<tr><td>sessionTimeoutMilliseconds</td><td>int</td><td>否</td><td>60000</td><td>会话超时时间<br>单位：毫秒</td></tr>
<tr><td>connectionTimeoutMilliseconds</td><td>int</td><td>否</td><td>15000</td><td>连接超时时间<br>单位：毫秒</td></tr>
<tr><td>digest</td><td>String</td><td>否</td><td>无权限验证</td><td>连接Zookeeper的权限令牌<br>缺省为不需要权限验证</td></tr>
</tbody>
</table>

* **基于Spring但不使用命名空间**

```xml
    <!-- 配置作业注册中心 -->
    <bean id="regCenter" class="com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter" init-method="init">
        <constructor-arg>
            <bean class="com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration">
                <property name="serverLists" value="${xxx}" />
                <property name="namespace" value="${xxx}" />
                <property name="baseSleepTimeMilliseconds" value="${xxx}" />
                <property name="maxSleepTimeMilliseconds" value="${xxx}" />
                <property name="maxRetries" value="${xxx}" />
            </bean>
        </constructor-arg>
    </bean>
    <!-- 配置作业-->
    <bean id="xxxJob" class="com.dangdang.ddframe.job.spring.schedule.SpringJobController" init-method="init">
        <constructor-arg ref="regCenter" />
        <constructor-arg>
            <bean class="com.dangdang.ddframe.job.api.JobConfiguration">
                <constructor-arg name="jobName" value="xxxJob" />
                <constructor-arg name="jobClass" value="xxxDemoJob" />
                <constructor-arg name="shardingTotalCount" value="10" />
                <constructor-arg name="cron" value="0/10 * * * * ?" />
                <property name="shardingItemParameters" value="${xxx}" />
            </bean>
        </constructor-arg>
    </bean>
```

* **不使用Spring配置**

如果不使用Spring框架，可以用如下方式启动作业。

``` java
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.schedule.JobInitializer;
import com.dangdang.ddframe.job.schedule.JobScheduler;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;

public class JobDemo {
  
    // 声明Zookeeper注册中心配置对象
    private final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("yourhost:2181", "zkRegTestCenter", 1000, 3000, 3);
    
    // 定义Zookeeper注册中心
    private final CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    // 声明作业配置对象
    private final JobConfiguration jobConfig = new JobConfiguration("demoJob", DemoJob.class, 3, "0/1 * * * * ?");
    
    public static void main(final String[] args) {
        new JobDemo().init();
    }
    
    public void init() {
        // 连接注册中心
        regCenter.init();
        // 声明作业启动器
        JobInitializer jobInitializer = new JobInitializer(regCenter, jobConfig);
        // 启动作业，主要是注册作业的信息
        jobInitializer.init();
        // 获取作业调度器实例
        JobScheduler jobScheduler = JobScheduler.getInstance();
        // 添加作业到调度器
        jobScheduler.addJob("demoJob", jobInitializer);
        // 启动所有作业
        jobScheduler.scheduleAllJobs();
    }
}
```

##使用限制
* 作业一旦启动成功后不能修改作业名称，如果修改名称则视为新的作业。
* 同一台作业服务器只能运行一个相同的作业实例，因为作业运行时是按照IP注册和管理的。
* 作业根据/etc/hosts文件获取IP地址，如果获取的IP地址是127.0.0.1而非真实IP地址，应正确配置此文件。
* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的Perpetual以及SequencePerpetual作业再执行完本次作业后不再继续执行，等待分片结束后再恢复正常。
* 开启monitorExecution才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但monitorExecution对短时间内执行的作业（如每5秒一触发）性能影响较大，建议关闭并自行实现幂等性。
* elastic-job没有自动删除作业服务器的功能，因为无法区分是服务器崩溃还是正常下线。所以如果要下线服务器，需要手工删除zookeeper中相关的服务器节点。由于直接删除服务器节点风险较大，暂时不考虑在运维平台增加此功能。

## 运维平台

* **登录**

  默认用户名和密码是**root/root**，可以通过修改conf\auth.properties文件修改默认登录用户名和密码。
  
* **主要功能**

  登录安全控制
  
  注册中心管理
  
  作业维度状态查看
  
  服务器维度状态查看
  
  快捷修改作业设置
  
  控制作业暂停和恢复运行

* **设计理念**

  运维平台和elastic-job并无直接关系，是通过读取作业注册中心数据展现作业状态，或更新注册中心数据修改全局配置。
  
  控制台只能控制作业本身是否运行，但不能控制作业进程的启停，因为控制台和作业本身服务器是完全分布式的，控制台并不能控制作业服务器。

* **不支持项**

  添加作业。因为作业都是在首次运行时自动添加，使用运维平台添加作业并无必要。
  
  停止作业。即使删除了Zookeeper信息也不能真正停止作业的运行，还会导致运行中的作业出问题。
  
  删除作业服务器。由于直接删除服务器节点风险较大，暂时不考虑在运维平台增加此功能。

* **主要界面**

  总览页
  
  ![总览页](http://static.oschina.net/uploads/space/2015/0914/215139_rVBi_719192.png)

  注册中心管理页
  
  ![注册中心管理页](http://static.oschina.net/uploads/space/2015/0914/215159_mbew_719192.png)

  作业详细信息页
  
  ![作业详细信息页](http://static.oschina.net/uploads/space/2015/0914/215232_Lj4d_719192.png)

  服务器详细信息页
  
  ![服务器详细信息页](http://static.oschina.net/uploads/space/2015/0914/215302_d3iw_719192.png)

## 实现原理

* **弹性分布式实现**

  1.	第一台服务器上线触发主服务器选举。主服务器一旦下线，则重新触发选举，选举过程中阻塞，只有主服务器选举完成，才会执行其他任务。

  2.	某作业服务器上线时会自动将服务器信息注册到注册中心，下线时会自动更新服务器状态。

  3.	主节点选举，服务器上下线，分片总数变更均更新重新分片标记。

  4.	定时任务触发时，如需重新分片，则通过主服务器分片，分片过程中阻塞，分片结束后才可执行任务。如分片过程中主服务器下线，则先选举主服务器，再分片。

  5.	通过4可知，为了维持作业运行时的稳定性，运行过程中只会标记分片状态，不会重新分片。分片仅可能发生在下次任务触发前。

  6.	每次分片都会按服务器IP排序，保证分片结果不会产生较大波动。

  7.	实现失效转移功能，在某台服务器执行完毕后主动抓取未分配的分片，并且在某台服务器下线后主动寻找可用的服务器执行任务。


* **注册中心数据结构**

  注册中心在定义的命名空间下，创建作业名称节点，用于区分不同作业，所以作业一旦创建则不能修改作业名称，如果修改名称将视为新的作业。作业名称节点下又包含4个数据子节点，分别是config, servers, execution和leader。
  
  **概览**

![注册中心数据结构](http://static.oschina.net/uploads/space/2015/0914/171533_1BOb_719192.png)


  **config节点**

  作业全局配置信息
  
<table>
<tbody>
<tr><td><em>子节点名</em></td><td><em>临时节点</em></td><td><em>描述</em></td></tr>
<tr><td>jobClass</td><td>否</td><td>作业实现类名称</td></tr>
<tr><td>shardingTotalCount</td><td>否</td><td>作业分片总数</td></tr>
<tr><td>cron</td><td>否</td><td>作业启动时间的cron表达式</td></tr>
<tr><td>shardingItemParameters</td><td>否</td><td>分片序列号和个性化参数对照表</td></tr>
<tr><td>jobParameter</td><td>否</td><td>作业自定义参数</td></tr>
<tr><td>monitorExecution</td><td>否</td><td>监控作业执行时状态</td></tr>
<tr><td>processCountIntervalSeconds</td><td>否</td><td>统计作业处理数据数量的间隔时间</td></tr>
<tr><td>concurrentDataProcessThreadCount</td><td>否</td><td>同时处理数据的并发线程数</td></tr>
<tr><td>fetchDataCount</td><td>否</td><td>每次抓取的数据量</td></tr>
<tr><td>failover</td><td>否</td><td>是否开启失效转移</td></tr>
<tr><td>description</td><td>否</td><td>作业描述信息</td></tr>
</tbody>
</table>


  **servers节点**

  作业服务器信息，子节点是作业服务器的IP地址。IP地址节点的子节点存储详细信息。同一台作业服务器只能运行一个相同的作业实例，因为作业运行时是按照IP注册和管理的。
  
<table>
<tbody>
<tr><td><em>子节点名</em></td><td><em>临时节点</em></td><td><em>描述</em></td></tr>
<tr><td>hostName</td><td>否</td><td>作业服务器名称</td></tr>
<tr><td>status</td><td>是</td><td>作业服务器状态，分为READY和RUNNING<br>用于表示服务器在等待执行作业还是正在执行作业<br>如果status节点不存在则表示作业服务器未上线</td></tr>
<tr><td>disabled</td><td>否</td><td>作业服务器状态是否禁用<br>可用于部署作业时，先禁止启动，部署结束后统一启动</td></tr>
<tr><td>sharding</td><td>否</td><td>该作业服务器分到的作业分片项<br>多个分片项用逗号分隔<br>如：0,1,2代表该服务器执行第1，2，3片分片</td></tr>
<tr><td>processSuccessCount</td><td>否</td><td>统计一段时间内处理数据成功的数量<br>统计间隔可通过config\processCountIntervalSeconds配置</td></tr>
<tr><td>processFailureCount</td><td>否</td><td>统计一段时间内处理数据失败的数量<br>统计间隔可通过config\processCountIntervalSeconds配置</td></tr>
<tr><td>stoped</td><td>否</td><td>停止作业的标记</td></tr>
</tbody>
</table>


  **execution节点**

  执行时信息，子节点是分片项序号，从零开始，至分片总数减一。分片项序号的子节点存储详细信息。可通过配置config\monitorExecution为false关闭记录作业执行时信息。
  
<table>
<tbody>
<tr><td><em>子节点名</em></td><td><em>临时节点</em></td><td><em>描述</em></td></tr>
<tr><td>running</td><td>是</td><td>分片项正在运行的状态<br>如果没有此节点，并且没有completed节点，表示该分片未运行</td></tr>
<tr><td>completed</td><td>否</td><td>分片项运行完成的状态<br>下次作业开始执行时会清理</td></tr>
<tr><td>failover</td><td>是</td><td>如果该分片项被失效转移分配给其他作业服务器，则此节点值记录执行此分片的作业服务器IP</td></tr>
<tr><td>lastBeginTime</td><td>否</td><td>该分片项最近一次的开始执行时间</td></tr>
<tr><td>nextFireTime</td><td>否</td><td>该分片项下次作业触发时间</td></tr>
<tr><td>lastCompleteTime</td><td>否</td><td>该分片项最近一次的结束执行时间</td></tr>
</tbody>
</table>


  **leader节点**

  作业服务器主节点信息，分为election，sharding和execution三个子节点。分别用于主节点选举，分片和作业执行时处理。
  
  leader节点是内部使用的节点，如果对作业框架原理不感兴趣，可不关注此节点。

  
<table>
<tbody>
<tr><td><em>子节点名</em></td><td><em>临时节点</em></td><td><em>描述</em></td></tr>
<tr><td>election\host</td><td>是</td><td>主节点服务器IP地址<br>一旦该节点被删除将会触发重新选举<br>重新选举的过程中一切主节点相关的操作都将阻塞</td></tr>
<tr><td>election\latch</td><td>否</td><td>主节点选举的分布式锁<br>为curator的分布式锁使用</td></tr>
<tr><td>sharding\necessary</td><td>否</td><td>是否需要重新分片的标记<br>如果分片总数变化，或作业服务器节点上下线或启用/禁用，以及主节点选举，会触发设置重分片标记<br>作业在下次执行时使用主节点重新分片，且中间不会被打断<br>作业执行时不会触发分片</td></tr>
<tr><td>sharding\processing</td><td>是</td><td>主节点在分片时持有的节点<br>如果有此节点，所有的作业执行都将阻塞，直至分片结束<br>主节点分片结束或主节点崩溃会删除此临时节点</td></tr>
<tr><td>execution\necessary</td><td>否</td><td>是否需要修正作业执行时分片项信息的标记<br>如果分片总数变化，会触发设置修正分片项信息标记<br>作业在下次执行时会增加或减少分片项数量</td></tr>
<tr><td>execution\cleaning</td><td>是</td><td>主节点在清理上次作业运行时状态时所持有的节点<br>每次开始新作业都需要清理上次运行完成的作业信息<br>如果有此节点，所有的作业执行都将阻塞，直至清理结束<br>主节点分片结束或主节点崩溃会删除此临时节点</td></tr>
<tr><td>failover\items\分片项</td><td>否</td><td>一旦有作业崩溃，则会向此节点记录<br>当有空闲作业服务器时，会从此节点抓取需失效转移的作业项</td></tr>
<tr><td>failover\items\latch</td><td>否</td><td>分配失效转移分片项时占用的分布式锁<br>为curator的分布式锁使用</td></tr>
</tbody>
</table>


* **流程图**

  作业启动
  
  ![作业启动](http://static.oschina.net/uploads/space/2015/0914/181007_yQ7b_719192.jpg)
  
  作业执行
  
  ![作业执行](http://static.oschina.net/uploads/space/2015/0914/181025_OSzr_719192.png)
