+++
title = "Dump Job Information"
weight = 2
chapter = true
+++

Using ElasticJob may meet some distributed problem which is not easy to observe.

Because of developer cannot debug in production environment, ElasticJob provide `dump` command to export job runtime information for debugging.

For security reason, the information dumped had already mask sensitive information, it instead of real IP address to `ip1`, `ip2` ...  

## Open Listener Port

Using Java API please refer to [Java API usage](/en/user-manual/elasticjob-lite/usage/job-api/java-api) for more details.
Using Spring please refer to [Spring usage](/en/user-manual/elasticjob-lite/usage/job-api/spring-namespace) for more details.

## Execute Dump

**Dump to stdout**

```bash
echo "dump@jobName" | nc <job server IP address> 9888
```

![Dump](https://shardingsphere.apache.org/elasticjob/current/img/dump/dump.jpg)

**Dump to file**

```bash
echo "dump@jobName" | nc <job server IP address> 9888 > job_debug.txt
```
