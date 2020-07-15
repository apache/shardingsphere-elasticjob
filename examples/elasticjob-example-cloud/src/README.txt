部署步骤
启动Zookeeper, Mesos Master/Agent 以及 ElasticJob-Cloud Scheduler。

将打包之后的作业tar.gz文件放至网络可访问的位置，如：ftp或http。打包的tar.gz文件中Main方法需要调用 ElasticJob-Cloud 提供的JobBootstrap.execute方法。

使用curl命令调用RESTful API注册作业。

RESTful API: ElasticJob-Cloud 提供作业及应用注册/注销RESTful API，可通过curl操作。

a.注册应用
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"foo_app","appURL":"http://app_host:8080/yourJobs.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true}' http://elastic_job_cloud_host:8899/api/app

b. 注册作业
注册的作业可用Java和Spring两种启动方式，作业启动在开发指南中有说明，这里只举例说明两种方式如何注册。
1. Java启动方式作业注册
curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"foo_job","jobClass":"yourJobClass","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appName":"foo_app","failover":true,"misfire":true}' http://elastic_job_cloud_host:8899/api/job/register

2. Spring启动方式作业注册
curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"foo_job","beanName":"yourBeanName","applicationContext":"applicationContext.xml","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appName":"foo_app","failover":true,"misfire":true}' http://elastic_job_cloud_host:8899/api/job/register

参数详细配置请见：http://elasticjob.io/elastic-job/elastic-job-cloud/02-guide/cloud-restful-api/


注册demo作业的快捷命令:

1. 注册APP:
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"exampleApp","appURL":"http://localhost:8080/elasticjob-example-cloud-2.1.4.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true}' http://localhost:8899/api/app

2. Java启动方式作业注册:

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_simple","appName":"exampleApp","jobExecutionType":"TRANSIENT","jobClass":"com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob","cron":"0/10 * * * * ?","shardingTotalCount":1,"cpuCount":0.1,"memoryMB":64.0}' http://localhost:8899/api/job/register

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_dataflow","appName":"exampleApp","jobExecutionType":"DAEMON","jobClass":"com.dangdang.ddframe.job.example.job.dataflow.JavaDataflowJob","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0}' http://localhost:8899/api/job/register

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_script","appName":"exampleApp","jobExecutionType":"TRANSIENT","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0, scriptCommandLine="script/demo.sh"}' http://localhost:8899/api/job/register

3. Spring启动方式作业注册:

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_simple_spring","appName":"exampleApp","jobExecutionType":"TRANSIENT","jobClass":"com.dangdang.ddframe.job.example.job.simple.SpringSimpleJob","beanName":"springSimpleJob","applicationContext":"classpath:META-INF/applicationContext.xml","cron":"0/10 * * * * ?","shardingTotalCount":1,"cpuCount":0.1,"memoryMB":64.0}' http://localhost:8899/api/job/register

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_dataflow_spring","appName":"exampleApp","jobExecutionType":"DAEMON","jobClass":"com.dangdang.ddframe.job.example.job.dataflow.SpringDataflowJob","beanName":"springDataflowJob","applicationContext":"classpath:META-INF/applicationContext.xml","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0}' http://localhost:8899/api/job/register
