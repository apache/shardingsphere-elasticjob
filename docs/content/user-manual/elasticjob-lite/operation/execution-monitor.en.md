+++
title = "Execution Monitor"
weight = 3
chapter = true
+++

By monitoring several key nodes in the zookeeper registry of ElasticJob-Lite, the job running status monitoring function can be completed.

## Monitoring job server alive

Listen for the existence of node job_name\instances\job_instance_id. This node is a temporary node. If the job server is offline, the node will be deleted.
