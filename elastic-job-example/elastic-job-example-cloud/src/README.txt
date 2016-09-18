部署步骤
启动Zookeeper, Mesos Master/Agent以及Elastic-Job-Cloud-Scheduler。

将打包之后的作业tar.gz文件放至网络可访问的位置，如：ftp或http。打包的tar.gz文件中Main方法需要调用Elastic-Job-Cloud提供的JobBootstrap.execute方法。

使用curl命令调用RESTful API注册作业。

RESTful API:Elastic-Job-Cloud提供作业注册/注销Restful API，可通过curl操作。

a.注册作业
url：job/register
方法：POST
参数类型：application/json
参数列表：

属性名	            类型	    是否必填	缺省值	描述
jobName	            String	  是		        作业名称。为Elastic-Job-Cloud的作业唯一标识
jobClass	        String	  是		        作业实现类
jobType	            Enum	  是		        作业类型。SIMPLE，DATAFLOW，SCRIPT
jobExecutionType    Enum	  是		        作业执行类型。TRANSIENT为瞬时作业，DAEMON为常驻作业
cron	            String	  是	           	cron表达式，用于配置作业触发时间
shardingTotalCount	int	      是		        作业分片总数
cpuCount	        double	  是		        单片作业所需要的CPU数量
memoryMB	        double	  是		        单片作业所需要的内存MB
appURL	            String	  是		        应用所在路径。必须是可以通过网络访问到的路径
bootstrapScript	    String	  是		        启动脚本，如：bin\start.sh。
failover        	boolean	  否	    false	是否开启失效转移
misfire	            boolean	  否 	false	是否开启错过任务重新执行
beanName	        String	  否		        Spring容器中配置的bean名称
applicationContext	String	  否		        Spring方式配置Spring配置文件相对路径以及名称，如：META-INF\applicationContext.xml
jobEventConfigs	    String	  否		        作业事件配置，目前可配置log和rdb监听器，如:{"log":{},"rdb":{"driverClassName":"com.mysql.jdbc.Driver", "url":"jdbc:mysql://your_host:3306/elastic_job_log", "username":"root", "password":"", "logLevel":"WARN"}}
scriptCommandLine	String	  否		        SCRIPT类型作业命令行执行脚本

注册的作业可用Java和Spring两种启动方式，作业启动在开发指南中有说明，这里只举例说明两种方式如何注册。
1.Java启动方式作业注册
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://app_host:8080/foo-job.tar.gz","failover":true,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_host:8899/job/register

2.Spring启动方式作业注册
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","beanName":"yourBeanName","applicationContext":"applicationContext.xml","jobType":"SIMPLE","jobExecutionType":"TRANSIENT",
"cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://file_host:8080/foo-job.tar.gz","failover":false,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_masterhost:8899/job/register

b.注销作业
url：job/deregister
方法：DELETE
参数类型：application/json
参数：作业名称

curl -l -H "Content-type: application/json" -X DELETE -d 'foo_job' http://elastic_job_cloud_host:8899/job/deregister