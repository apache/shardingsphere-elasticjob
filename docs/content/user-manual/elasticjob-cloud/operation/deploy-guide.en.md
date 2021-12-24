+++
title = "Deploy Guide"
weight = 1
chapter = true
+++

## Scheduler deployment steps

1. Start ElasticJob-Cloud-Scheduler and Mesos, and specify ZooKeeper as the registry.
2. Start Mesos Master and Mesos Agent.
3. Unzip `elasticjob-cloud-scheduler-${version}.tar.gz`.
4. Run `bin\start.sh` to start ElasticJob-Cloud-Scheduler.

## Job deployment steps

1. Ensure that ZooKeeper, Mesos Master/Agent and ElasticJob-Cloud-Scheduler have been started correctly.
2. Place the tar.gz file of the packaging job in a network accessible location, such as ftp or http. The `main` method in the packaged tar.gz file needs to call the `JobBootstrap.execute` method provided by ElasticJob-Cloud.
3. Use curl command to call RESTful API to publish applications and register jobs. For details: [Configuration](/en/user-manual/elasticjob-cloud/configuration)

## Scheduler configuration steps

Modify the `conf\elasticjob-cloud-scheduler.properties` to change the system configuration.

Configuration description:

| Attribute Name           | Required | Default                  | Description                                                                                 |
| ------------------------ |:-------  |:------------------------- |:------------------------------------------------------------------------------------------ |
| hostname                 | yes      |                           | The real IP or hostname of the server, cannot be 127.0.0.1 or localhost                    |
| user                     | no       |                           | User name used by Mesos framework                                                          |
| mesos_url                | yes      | zk://127.0.0.1:2181/mesos | Zookeeper url used by Mesos                                                                |
| zk_servers               | yes      | 127.0.0.1:2181            | Zookeeper address used by ElasticJob-Cloud                                                 |
| zk_namespace             | no       | elasticjob-cloud          | Zookeeper namespace used by ElasticJob-Cloud                                               |
| zk_digest                | no       |                           | Zookeeper digest used by ElasticJob-Cloud                                                  |
| http_port                | yes      | 8899                      | Port used by RESTful API                                                                   |
| job_state_queue_size     | yes      | 10000                     | The maximum value of the accumulation job, the accumulation job exceeding this threshold will be discarded. Too large value may cause ZooKeeper to become unresponsive, and should be adjusted according to the actual measurement |
| event_trace_rdb_driver   | no       |                           | Driver of Job event tracking database                                                      |
| event_trace_rdb_url      | no       |                           | Url of Job event tracking database                                                         |
| event_trace_rdb_username | no       |                           | Username of Job event tracking database                                                    |
| event_trace_rdb_password | no       |                           | Password of Job event tracking database                                                     |
| auth_username            | no       | root                      | API authentication username                                                                |
| auth_password            | no       | pwd                       | API authentication password                                                                |

***

* Stop: No stop script is provided, you can directly use the kill command to terminate the process.
