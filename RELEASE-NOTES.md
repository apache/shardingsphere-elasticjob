## 3.0.0.M1

### Bug Fixes

1. [ISSUE #384](https://github.com/elasticjob/elastic-job/issues/384) Cloud's executor thread ContextClassLoader is empty


## 2.1.5

### New Features

1. [ISSUE #373](https://github.com/elasticjob/elastic-job/issues/373) Cloud can distinguishes processing TASK_UNREACHABLE,TASK_UNKNOWN,TASK_DROPPED,TASK_GONE,etc

### Bug Fixes

1. [ISSUE #367](https://github.com/elasticjob/elastic-job/issues/367) Massive stacked jobs performed after Cloud restart because disabled job does not stop Ready queue
1. [ISSUE #382](https://github.com/elasticjob/elastic-job/issues/382) UI verification error, maximum number of shards should not be verified
1. [ISSUE #383](https://github.com/elasticjob/elastic-job/issues/383) UI verification error, minimum number of listening port should not be verified

## 2.1.4

### Enhancement

1. [ISSUE #29](https://github.com/elasticjob/elastic-job/issues/29)   Console support english
1. [ISSUE #352](https://github.com/elasticjob/elastic-job/issues/352) Running elastic-job-cloud-executor locally without mesos environment

### Bug Fixes

1. [ISSUE #322](https://github.com/elasticjob/elastic-job/issues/322) Schedule tasks to evaluate resources when considering the use of resources for executor in elastic-job-cloud-scheduler module
1. [ISSUE #341](https://github.com/elasticjob/elastic-job/issues/341) Script task configuration in elastic-job-cloud-console is missing execution script
1. [ISSUE #343](https://github.com/elasticjob/elastic-job/issues/343) Script task execution script is incorrect in elastic-job-cloud-console module
1. [ISSUE #345](https://github.com/elasticjob/elastic-job/issues/345) The status is not displayed correctly when the task is all disabled in elastic-job-lite-console module
1. [ISSUE #351](https://github.com/elasticjob/elastic-job/issues/351) Manage background add registry, login credentials bar can not enter ':' in elastic-job-lite-console module

## 2.1.3

### Enhancement

1. [ISSUE #327](https://github.com/elasticjob/elastic-job/issues/327) spring namespace supports use xml to config beans
1. [ISSUE #336](https://github.com/elasticjob/elastic-job/issues/336) Cloud task submission failure returns error details to framework

### Bug Fixes

1. [ISSUE #321](https://github.com/elasticjob/elastic-job/issues/321) elastic-job-lite The namespace is not support / when UI adds the registry
1. [ISSUE #333](https://github.com/elasticjob/elastic-job/issues/333) elastic-job-lite Registration center configuration login credentials in the UI implicit display
1. [ISSUE #334](https://github.com/elasticjob/elastic-job/issues/334) elastic-job-lite UI can't find conf\auth.properties file on windows platform
1. [ISSUE #335](https://github.com/elasticjob/elastic-job/issues/335) elastic-job-lite UI guest account configuration does not work in conf\auth.properties file

## 2.1.2

### New Features

1. [ISSUE #301](https://github.com/elasticjob/elastic-job/issues/301) Console add guest permission configuration, guest only allows viewing, not allowed to change
1. [ISSUE #312](https://github.com/elasticjob/elastic-job/issues/312) Cloud support self-healing

### Enhancement

1. [ISSUE #293](https://github.com/elasticjob/elastic-job/issues/293) Lite Console datasource configuration adds connection testing capabilities
1. [ISSUE #296](https://github.com/elasticjob/elastic-job/issues/296) Cloud operational UI refactoring, consistent with lite style
1. [ISSUE #302](https://github.com/elasticjob/elastic-job/issues/302) Failure transfer and task run state monitoring separation
1. [ISSUE #304](https://github.com/elasticjob/elastic-job/issues/304) Cloud add associated features with Mesos roles
1. [ISSUE #316](https://github.com/elasticjob/elastic-job/issues/316) Lite running task association process ID

### Bug Fixes

1. [ISSUE #291](https://github.com/elasticjob/elastic-job/issues/291) elastic-job console failure reason display is not complete
1. [ISSUE #306](https://github.com/elasticjob/elastic-job/issues/306) Switch whether to monitor job execution status and task intervals are short may occur when the task cannot continue to run
1. [ISSUE #310](https://github.com/elasticjob/elastic-job/issues/310) Create to many sequential nodes after configuration check time error seconds for this machine and registry

## 2.1.1

### New Features

1. [ISSUE #242](https://github.com/elasticjob/elastic-job/issues/242) Elastic-Job-Cloud supports delete application and task
1. [ISSUE #243](https://github.com/elasticjob/elastic-job/issues/243) Elastic-Job-Cloud supports enable/disable application and task

### Enhancement

1. [ISSUE #268](https://github.com/elasticjob/elastic-job/issues/268) Simplify POM dependency

### Bug Fixes

1. [ISSUE #266](https://github.com/elasticjob/elastic-job/issues/266) Elastic-Job-Lite start script specifies that the port is invalid
1. [ISSUE #269](https://github.com/elasticjob/elastic-job/issues/269) EventTrace failure record is not affected by sample rate and the time of failure is recorded
1. [ISSUE #270](https://github.com/elasticjob/elastic-job/issues/270) Console send two requests after clicks the button
1. [ISSUE #272](https://github.com/elasticjob/elastic-job/issues/272) Elastic-Job-Lite UI job dimensions that should appear as disabled only if all servers are disabled
1. [ISSUE #275](https://github.com/elasticjob/elastic-job/issues/275) After stopping Zookeeper, restart Zookeeper and the task does not continue
1. [ISSUE #276](https://github.com/elasticjob/elastic-job/issues/276) When fail transfer is turned on and the shard task is performed, the task is repeated
1. [ISSUE #279](https://github.com/elasticjob/elastic-job/issues/279) Add event tracking data source, database connection address can not have parameters
1. [ISSUE #280](https://github.com/elasticjob/elastic-job/issues/280) The historical status of the task history page is not displayed correctly
1. [ISSUE #283](https://github.com/elasticjob/elastic-job/issues/283) Task is not set overwrite and local configuration is inconsistent with the registration center, the cron started by the task shall be based on the registry
1. [ISSUE #290](https://github.com/elasticjob/elastic-job/issues/290) Elastic-Job-Cloud when deleting a disabled APP or JOB, the corresponding disabled node data cannot be deleted

## 2.1.0

### New Features

1. [ISSUE #195](https://github.com/elasticjob/elastic-job/issues/195) Elastic-Job-Lite self-diagnose and fix problems caused by distributed instability
1. [ISSUE #248](https://github.com/elasticjob/elastic-job/issues/248) Elastic-Job-Lite the same job server can run multiple JVM instances with the same job name(Cloud Native)
1. [ISSUE #249](https://github.com/elasticjob/elastic-job/issues/249) Elastic-Job-Lite Operations UI supports incident tracking queries

### Enhancement

1. [ISSUE #240](https://github.com/elasticjob/elastic-job/issues/240) Elastic-Job-Lite operational UI refactoring.
1. [ISSUE #262](https://github.com/elasticjob/elastic-job/issues/262) Elastic-Job-Lite console delete job configuration.

### Bug Fixes

1. [ISSUE #237](https://github.com/elasticjob/elastic-job/issues/238) Add the REST API check on the total number of shards not less than 1
1. [ISSUE #238](https://github.com/elasticjob/elastic-job/issues/238) IP regular expression error
1. [ISSUE #246](https://github.com/elasticjob/elastic-job/issues/246) After using JobOperateAPI.remove()，JobScheduler.init() triggers execution multiple times after creating the same job
1. [ISSUE #250](https://github.com/elasticjob/elastic-job/issues/250) Misfire task triggers more than once

### Refactor

1. [ISSUE #263](https://github.com/elasticjob/elastic-job/issues/263) Elastic-Job-Lite Job OperationAPI Re-grooming
1. [ISSUE #264](https://github.com/elasticjob/elastic-job/issues/264) Elastic-Job-Lite Data storage restructuring, but forward compatibility

## 2.0.5

### New Features

1. [ISSUE #191](https://github.com/elasticjob/elastic-job/issues/191) Framework's HA feature
1. [ISSUE #217](https://github.com/elasticjob/elastic-job/issues/217) cloud add APP dimension configuration
1. [ISSUE #223](https://github.com/elasticjob/elastic-job/issues/223) cloud resident job event tracking sample rate

### Bug Fixes

1. [ISSUE 222](https://github.com/elasticjob/elastic-job/issues/222) elastic-job-lite-spring reg configuration parameter max-retries does not work
1. [ISSUE 231](https://github.com/elasticjob/elastic-job/issues/231) When a cloud job is deleted in bulk, mesos synchronizes TASK_LOST message to the framework in advance, causing the job to be re-arranged in the ready queue and executed

## 2.0.4

### New Features

1. [ISSUE #203](https://github.com/elasticjob/elastic-job/issues/203) Cloud task add run statistics and provide REST API queries
1. [ISSUE #215](https://github.com/elasticjob/elastic-job/issues/215) cloud operations management UI

### Enhancement

1. [ISSUE #187](https://github.com/elasticjob/elastic-job/issues/187) ShardingContext add task attribute to business side

### Bug Fixes

1. [ISSUE #189](https://github.com/elasticjob/elastic-job/issues/189) Manage background to perform a failure operation, but the task is still being executed
1. [ISSUE #204](https://github.com/elasticjob/elastic-job/issues/204) Async execution of messages in consistency results in inaccurate database data
1. [ISSUE #209](https://github.com/elasticjob/elastic-job/issues/209) cloud task resource allocation algorithm improvement

## 2.0.3

### Refactor

1. [ISSUE #184](https://github.com/elasticjob/elastic-job/issues/184) ExecutorServiceHandler interface method adjustment, add jobName used to distinguish between different job thread names
1. [ISSUE #186](https://github.com/elasticjob/elastic-job/issues/186) Simplify SpringJobScheduler use by removing Spring Namespace DTO-related code

### New Features

1. [ISSUE #178](https://github.com/elasticjob/elastic-job/issues/178) Event-driven trigger jobs

### Enhancement

1. [ISSUE #179](https://github.com/elasticjob/elastic-job/issues/179) Transient's Script-type task optimization, no Java Executor support required
1. [ISSUE #182](https://github.com/elasticjob/elastic-job/issues/182) add support for spring boot

### Bug Fixes

1. [ISSUE #177](https://github.com/elasticjob/elastic-job/issues/177) Spring Namespace Job: Script Null Pointer in version 2.0.2
1. [ISSUE #185](https://github.com/elasticjob/elastic-job/issues/185) Executor over-occupancy of sharding resources leads to waste of resources

## 2.0.2

### Refactor

1. [ISSUE #153](https://github.com/elasticjob/elastic-job/issues/153) Centralization of event tracking configuration
1. [ISSUE #160](https://github.com/elasticjob/elastic-job/issues/160) Adjust the maven module structure to provide elastic-job-common and its secondary modules, the original elastic-job-core module migration to elastic-job-common-core

### Enhancement

1. [ISSUE #159](https://github.com/elasticjob/elastic-job/issues/159) Available in any version from Spring 3.1.0.RELEASE to Spring 4
1. [ISSUE #164](https://github.com/elasticjob/elastic-job/issues/164) JobBeans that have been declared in the job Spring namespace no longer need to declare @Component or define in Spring xml

### Bug Fixes

1. [ISSUE #64](https://github.com/elasticjob/elastic-job/issues/64)   Spring namespace, if you register multiple job beans of the same class, will cause job beans to look up inaccurately
1. [ISSUE #115](https://github.com/elasticjob/elastic-job/issues/115) Console add new registry, no connection success, back stage has been repeatedly connected and reported errors
1. [ISSUE #151](https://github.com/elasticjob/elastic-job/issues/151) Lack of support for relational database-based event tracking for databases outside MySQL
1. [ISSUE #152](https://github.com/elasticjob/elastic-job/issues/152) Job custom exception processor is invalid and is always handled by Default JobExceptionHandler
1. [ISSUE #156](https://github.com/elasticjob/elastic-job/issues/156) Job event tracking overall call link data acquisition
1. [ISSUE #158](https://github.com/elasticjob/elastic-job/issues/158) Job misses sharding when it is paused and will no longer shard
1. [ISSUE #161](https://github.com/elasticjob/elastic-job/issues/161) Version of Lite deployed to some versions of Tomcat cannot be started
1. [ISSUE #163](https://github.com/elasticjob/elastic-job/issues/163) The project is started or the task is automatically performed after the task is set to disable true
1. [ISSUE #165](https://github.com/elasticjob/elastic-job/issues/165) Shard thread deadlock when all service nodes are disable
1. [ISSUE #167](https://github.com/elasticjob/elastic-job/issues/167) Failover job adds task ID record

## 2.0.1

### Bug Fixes

1. [ISSUE #141](https://github.com/elasticjob/elastic-job/issues/141) Remove the reg module to read information from zk, making the reg namespace's placeholder fully available
1. [ISSUE #143](https://github.com/elasticjob/elastic-job/issues/143) elastic-job-cloud-scheduler memory leak
1. [ISSUE #145](https://github.com/elasticjob/elastic-job/issues/145) After modifying the database connection of the task log, the log is still written to the old database
1. [ISSUE #146](https://github.com/elasticjob/elastic-job/issues/146) Thread pool reuse problem for a task
1. [ISSUE #147](https://github.com/elasticjob/elastic-job/issues/147) console task does not load, background there is an null pointer exception
1. [ISSUE #149](https://github.com/elasticjob/elastic-job/issues/149) Operations platform delete tasks, occasionally encounter deletion incomplete situation
1. [ISSUE #150](https://github.com/elasticjob/elastic-job/issues/150) Cloud's misfire feature will be performed as jobs pile up

## 2.0.0

### New Features

1. Elastic-Job-Cloud initial version
1. Reconstruct the original Elastic-Job to Elastic-Job-Lite

### Bug Fixes

1. [ISSUE #119](https://github.com/elasticjob/elastic-job/issues/119) Quartz does not close properly when spring container is closed 
1. [ISSUE #123](https://github.com/elasticjob/elastic-job/issues/123) Stand-alone running timing task, zk disconnect after reconnecting, did not trigger the leader election
1. [ISSUE #127](https://github.com/elasticjob/elastic-job/issues/127) Spring configuration task id cannot use placeholders

## 1.1.1

### Refactor

1. [ISSUE #116](https://github.com/elasticjob/elastic-job/issues/116) HandleJobExecutionException parameter changes for job interface

### Enhancement

1. [ISSUE #110](https://github.com/elasticjob/elastic-job/issues/110) Trigger the task manually

### Bug Fixes
1. [ISSUE #99](https://github.com/elasticjob/elastic-job/issues/99) After deleting a task asynchronously caused the job to be deleted, the task that has not yet ended continues to create zk data

## 1.1.0

### Refactor

1. [ISSUE #97](https://github.com/elasticjob/elastic-job/issues/97)   JobConfiguration Refactored to SimpleJobConfiguration，DataflowJobConfiguration，ScriptJobConfiguration
1. [ISSUE #102](https://github.com/elasticjob/elastic-job/issues/102) Redefine Java/Spring Config API，replace Constructor+Setter with Factory+Builder model
1. [ISSUE #104](https://github.com/elasticjob/elastic-job/issues/104) Remove @Deprecated code
1. [ISSUE #105](https://github.com/elasticjob/elastic-job/issues/105) Reconstructing the Spring Namespace Hump Definition
1. [ISSUE #106](https://github.com/elasticjob/elastic-job/issues/106) isStreaming Configuration
1. [ISSUE #107](https://github.com/elasticjob/elastic-job/issues/107) reg-center renamed registry-center-ref

## 1.0.8

### New Features

1. [ISSUE #95](https://github.com/elasticjob/elastic-job/issues/95) Add script type job support

## 1.0.7

### Refactor

1. [ISSUE #88](https://github.com/elasticjob/elastic-job/issues/88) Stop task renamed pause

### New Features

1. [ISSUE #91](https://github.com/elasticjob/elastic-job/issues/91) Job Lifecycle Action API

### Enhancement

1. [ISSUE #84](https://github.com/elasticjob/elastic-job/issues/84) The console provides job enable/disable button action
1. [ISSUE #87](https://github.com/elasticjob/elastic-job/issues/87) Adjusting the master node election process, job shutdown, disabling and pausing will trigger the master node election
1. [ISSUE #93](https://github.com/elasticjob/elastic-job/issues/93) The registry configuration provides default values for baseSleepTimeMilliseconds, maxSleepTimeMilliseconds, and maxRetries

### Bug Fixes
1. [ISSUE #92](https://github.com/elasticjob/elastic-job/issues/92) Modifying the total shard parameter results in a listening throw timeout exception performed by only a single node

## 1.0.6

### Enhancement

1. [ISSUE #71](https://github.com/elasticjob/elastic-job/issues/71) Task off function（shutdown）
1. [ISSUE #72](https://github.com/elasticjob/elastic-job/issues/72) Closed jobs can be deleted
1. [ISSUE #81](https://github.com/elasticjob/elastic-job/issues/81) Using the last end state of a centralized cleanup job instead of the respective cleanup, each cleaning may result in an uncleaned end state due to offline

### Bug Fixes

1. [ISSUE #74](https://github.com/elasticjob/elastic-job/issues/74) When streaming and fail transfer, the failover shard item cannot be executed once and stopped
1. [ISSUE #77](https://github.com/elasticjob/elastic-job/issues/77) Dataflow type task, fetchData if there is data, should be executed in pairs with processData
1. [ISSUE #78](https://github.com/elasticjob/elastic-job/issues/78) Spring configuration job monitoring enable AOP causes problems that do not work properly

## 1.0.5

### Refactor

1. [ISSUE #59](https://github.com/elasticjob/elastic-job/issues/59) elastic-job upgrade curator from 2.8.0 to 2.10.0

### Enhancement

1. [ISSUE #2](https://github.com/elasticjob/elastic-job/issues/2)   Add front and post tasks
1. [ISSUE #60](https://github.com/elasticjob/elastic-job/issues/60) Dataflow type task customized thread pool configuration
1. [ISSUE #62](https://github.com/elasticjob/elastic-job/issues/61) Job status cleanup speed-up
1. [ISSUE #65](https://github.com/elasticjob/elastic-job/issues/65) Add spring namespace support for front and post tasks

### Bug Fixes

1. [ISSUE #61](https://github.com/elasticjob/elastic-job/issues/61) Deadlock problem solved when sharding and primary node elections occur at the same time
1. [ISSUE #63](https://github.com/elasticjob/elastic-job/issues/63) You may get TreeCache for other jobs with the same prefix when you get the job TreeCache
1. [ISSUE #69](https://github.com/elasticjob/elastic-job/issues/69) If the job server sharding node in Zk does not exist when sharding, it will not be able to reshard

## 1.0.4

### Refactor

1. [ISSUE #57](https://github.com/elasticjob/elastic-job/issues/57) Thin module, remove elastic-job-test module
1. [ISSUE #58](https://github.com/elasticjob/elastic-job/issues/58) Add changes in job type interfaces due to bulk processing capabilities

### Enhancement

1. [ISSUE #16](https://github.com/elasticjob/elastic-job/issues/16) Provides embedded zookeeper to simplify the development environment
1. [ISSUE #28](https://github.com/elasticjob/elastic-job/issues/28) Dataflow type tasks to increase processData bulk processing of data
1. [ISSUE #56](https://github.com/elasticjob/elastic-job/issues/56) Job custom parameter settings

## 1.0.3

### Enhancement

1. [ISSUE #39](https://github.com/elasticjob/elastic-job/issues/39) Add job assisted listening and fetch job runtime information with dump command
1. [ISSUE #43](https://github.com/elasticjob/elastic-job/issues/43) Add job exception handling callback interface

### Bug Fixes

1. [ISSUE #30](https://github.com/elasticjob/elastic-job/issues/30) Registry is down for a long time and resumes, and the job cannot continue
1. [ISSUE #36](https://github.com/elasticjob/elastic-job/issues/36) Task cannot resume after console pause
1. [ISSUE #40](https://github.com/elasticjob/elastic-job/issues/40) TreeCache uses Coarse granularity cause memory overflow

## 1.0.2

### Refactor

1. [ISSUE #17](https://github.com/elasticjob/elastic-job/issues/17) Task type interface changes

### Enhancement

1. [ISSUE #6](https://github.com/elasticjob/elastic-job/issues/6)   Proofreading job server and registry time error
1. [ISSUE #8](https://github.com/elasticjob/elastic-job/issues/8)   Increase misfire switch, default enable missed task re-execution
1. [ISSUE #9](https://github.com/elasticjob/elastic-job/issues/9)   Sharding policy configurability
1. [ISSUE #10](https://github.com/elasticjob/elastic-job/issues/10) Provides a sorting strategy for odd even shards based on job name hash value
1. [ISSUE #14](https://github.com/elasticjob/elastic-job/issues/14) When the console modifies the cron expression, the task updates the cron in real time
1. [ISSUE #20](https://github.com/elasticjob/elastic-job/issues/20) Operations UI task list shows increased cron expression
1. [ISSUE #54](https://github.com/elasticjob/elastic-job/issues/54) Sequenceperpetual task performance improved, changing fetch data to multithreaded, previously processing data only as multithreaded
1. [ISSUE #55](https://github.com/elasticjob/elastic-job/issues/55) offset storage capabilities

### Bug Fixes

1. [ISSUE #1](https://github.com/elasticjob/elastic-job/issues/1)   Inaccurate access to IP addresses in complex network environments
1. [ISSUE #13](https://github.com/elasticjob/elastic-job/issues/13) After a job throws a run-time exception, it does not continue to be triggered later
1. [ISSUE #53](https://github.com/elasticjob/elastic-job/issues/53) Dataflow's Sequence type tasks use multithreaded fetch data

## 1.0.1
1. Initial version
