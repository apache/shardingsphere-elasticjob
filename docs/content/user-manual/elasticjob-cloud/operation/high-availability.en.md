+++
title = "High Available"
weight = 2
chapter = true
+++

## Introduction

The high availability of the scheduler is achieved by running several ElasticJob-Cloud-Scheduler instances pointing to the same ZooKeeper cluster.
ZooKeeper is used to perform leader election when the current primary ElasticJob-Cloud-Scheduler instance fails.
At least two scheduler instances are used to form a cluster. Only one scheduler instance in the cluster provides services, and the other instances are in the `standby` state.
When the instance fails, the cluster will elect one of the remaining instances to continue providing services.

## Configuration

Each ElasticJob-Cloud-Scheduler instance must use the same ZooKeeper cluster.
For example，if the Quorum of ZooKeeper is zk://1.2.3.4:2181,2.3.4.5:2181,3.4.5.6:2181/elasticjob-cloud，the ZooKeeper related configuration in `elasticjob-cloud-scheduler.properties` is:

```properties
# ElasticJob-Cloud's ZooKeeper address
zk_servers=1.2.3.4:2181,2.3.4.5:2181,3.4.5.6:2181

# ElasticJob-Cloud's ZooKeeper namespace
zk_namespace=elasticjob-cloud
```

