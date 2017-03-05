部署步骤
启动Zookeeper, Mesos Master/Agent以及Elastic-Job-Cloud-Scheduler。

将打包之后的作业tar.gz文件放至网络可访问的位置，如：ftp或http。打包的tar.gz文件中Main方法需要调用Elastic-Job-Cloud提供的JobBootstrap.execute方法。

使用curl命令调用RESTful API注册作业。

RESTful API:Elastic-Job-Cloud提供作业注册/注销RESTful API，可通过curl操作。

a. 注册作业
注册的作业可用Java和Spring两种启动方式，作业启动在开发指南中有说明，这里只举例说明两种方式如何注册。
1. Java启动方式作业注册
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://app_host:8080/foo-job.tar.gz","failover":true,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_host:8899/job/register

2. Spring启动方式作业注册
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","beanName":"yourBeanName","applicationContext":"applicationContext.xml","jobType":"SIMPLE","jobExecutionType":"TRANSIENT",
"cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://file_host:8080/foo-job.tar.gz","failover":false,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_masterhost:8899/job/register

b. 注销作业
curl -l -H "Content-type: application/json" -X DELETE -d 'foo_job' http://elastic_job_cloud_host:8899/job/deregister

参数详细配置请见：http://dangdangdotcom.github.io/elastic-job/post/user_guide/cloud/deploy_guide/


注册demo作业的快捷命令:

1. Java启动方式作业注册:

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_simple","jobType":"SIMPLE","jobClass":"com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT",
"appURL":"http://localhost:8080/elastic-job-example-cloud-2.0.3.tar.gz","failover":false,"misfire":true,"jobParameter":"java_simple","bootstrapScript":"bin/start.sh"}' http://localhost:8899/job/register

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_dataflow","jobType":"DATAFLOW","jobClass":"com.dangdang.ddframe.job.example.job.dataflow.JavaDataflowJob","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT",
"appURL":"http://localhost:8080/elastic-job-example-cloud-2.0.3.tar.gz","failover":false,"misfire":true,"jobParameter":"java_dataflow","bootstrapScript":"bin/start.sh"}' http://localhost:8899/job/register

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_script","jobType":"SCRIPT","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT",
"appURL":"http://localhost:8080/demo.sh","failover":false,"misfire":true,"jobParameter":"script","bootstrapScript":"./demo.sh"}' http://localhost:8899/job/register

2. Spring启动方式作业注册:

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_simple_spring","jobType":"SIMPLE","jobClass":"com.dangdang.ddframe.job.example.job.simple.SpringSimpleJob","beanName":"springSimpleJob","applicationContext":"classpath:META-INF/applicationContext.xml","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT",
"appURL":"http://localhost:8080/elastic-job-example-cloud-2.0.3.tar.gz","failover":false,"misfire":true,"jobParameter":"java_simple","bootstrapScript":"bin/start.sh"}' http://localhost:8899/job/register

curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"test_job_dataflow_spring","jobType":"DATAFLOW","jobClass":"com.dangdang.ddframe.job.example.job.dataflow.SpringDataflowJob","beanName":"springDataflowJob","applicationContext":"classpath:META-INF/applicationContext.xml","cron":"0/10 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT",
"appURL":"http://localhost:8080/elastic-job-example-cloud-2.0.3.tar.gz","failover":false,"misfire":true,"jobParameter":"java_simple","bootstrapScript":"bin/start.sh"}' http://localhost:8899/job/register
