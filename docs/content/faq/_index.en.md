+++
pre = "<b>8. </b>"
title = "FAQ"
weight = 8
chapter = true
+++

## 1. Why do some compiling errors appear?

Answer:

`ElasticJob` uses `lombok` to enable minimal coding. For more details about using and installment, please refer to the official website of [lombok](https://projectlombok.org/download).

## 2. Does ElasticJob support dynamically adding jobs?

Answer:

For the concept of dynamically adding job, everyone has a different understanding.

`ElasticJob-Lite` is provided in jar package, which is started by developers or operation. When the job is started, it will automatically register job information to the registry center, and the registry center will perform distributed coordination, so there is no need to manually add job information in the registry center.
However, registry center has no affiliation with the job server, can't control the distribution of single-point jobs to other job machines, and also can't start the job of remote server.
`ElasticJob-Lite` doesn't support ssh secret management and other functions.

`ElasticJob-Cloud` is a `mesos` framework, and `mesos` is responsible for job starting and distribution.
But you need to package the job and upload it, and call the `REST API` provided by `ElasticJob-Cloud` to write job information into the registry center.
Packaging and uploading job are the deployment system's functions, `ElasticJob-Cloud` does not support it.

In summary, `ElasticJob` has supported basic dynamically adding jobs, but it can't be fully automated.

## 3. Why is the job configuration modified in the code or Spring XML file, but the registry center is not updated?

Answer:

`ElasticJob-Lite` adopts a decentralized design. If the configuration of each client is inconsistent and is not controlled, the configuration of the client which is last started will be the final configuration of the registry center.

`ElasticJob-Lite` proposes the concept of `overwrite`, which can be configured through `JobConfiguration` or `Spring` namespace.
`overwrite=true` indicates that the client's configuration is allowed to override the registry center, and on the contrary is not allowed.
If there is no configuration of related jobs in the registry center, regardless of whether the property of `overwrite` is configured, the client's configuration will be still written into the registry center.

## 4. What happens if the job can't communicate with the registry center?

Answer:

In order to ensure the consistency of the job in the distributed system, once the job can't communicate with the registry center, the job will stop immediately, but the job's process will not exit.
The purpose of this is to prevent the assignment of the shards executed by the node that has lost contact with the registry center to another node when the job is re-sharded, causing the same shard to be executed on both nodes at the same time.
When the node resumes contact with the registry center, it will re-participate in the sharding and resume execution of the newly shard.

## 5. What are the usage restrictions of `ElasticJob-Lite`?

Answer:

* After the job start successfully, modifying the job name is regarded as a new job, and the original job is discarded.

* It will be triggered re-sharding if the server changes, or if the sharding item is modified; re-sharding will cause the running streaming job to stop after the job is executed, and this job will return to normal after the re-sharding is finished.

* Enable `monitorExecution` to realize the function of distributed job idempotence (that is, the same shard will not be run on different job servers), but `monitorExecution` has a greater impact on the performance of jobs executed in a short period of time (such as second-level triggers). It is recommended to turn it off and realize idempotence by yourself.

## 6. What should you do if you suspect that `ElasticJob-Lite` has a problem in a distributed environment, but it cannot be reproduced and cannot be debugged in the online environment?

Answer:

Distributed problems are very difficult to debug and reproduce. For this reason, `ElasticJob-Lite` provides the `dump` command.

If you suspect a problem in some scenarios, you can refer to the [dump](/en/user-manual/elasticjob-lite/operation/dump/) document to submit the job runtime information to the community.
`ElasticJob` has filtered sensitive information such as `IP`, and the dump file can be safely transmitted on the Internet.

## 7. What are the usage restrictions of `ElasticJob-Cloud`?

Answer:

* After the job start successfully, modifying the job name is regarded as a new job, and the original job is discarded.

## 8. When add a task in the `ElasticJob-Cloud`, why does it remain in the ready state, but doesn't start?

Answer:

The task will start when `mesos` has a separate `agent` that can provide the required resources, otherwise it will wait until there are enough resources.

## 9. Why can't the Console page display normally?

Answer:

Make sure that the `Web Console`'s version is consistent with `ElasticJob`, otherwise it will become unavailable.

## 10. Why is the job state shard to be adjusted in the Console?

Answer:

Shard to be adjusted indicates the state when the job has started but has not yet obtained the shard.

## 11. Why is there a task scheduling delay in the first startup?

Answer:

ElasticJob will obtain the local IP when performing task scheduling, and it may be slow to obtain the IP for the first time. Try to set -Djava.net.preferIPv4Stack=true.


## 12. In Windows env, run ShardingSphere-ElasticJob-UI, could not find or load main class org.apache.shardingsphere.elasticjob.lite.ui.Bootstrap. Why?

Answer:

Some decompression tools may truncate the file name when decompressing the ShardingSphere-ElasticJob-UI binary package, resulting in some classes not being found

Open cmd.exe and execute the following command:

tar zxvf apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-ui-bin.tar.gz

## 13. Unable to startup Cloud Scheduler. Continuously output "Elastic job: IP:PORT has leadership"gg

Answer: 

Cloud Scheduler required Mesos native library. Specify Mesos native library path by property `-Djava.library.path`.

For instance, Mesos native libraries are under `/usr/local/lib`, so the property `-Djava.library.path=/usr/local/lib` need to be set to start the Cloud Scheduler.

About Apache Mesos, please refer to [Apache Mesos](https://mesos.apache.org/).
