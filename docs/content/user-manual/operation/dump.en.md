+++
title = "Dump Job Information"
weight = 2
chapter = true
+++

Using ElasticJob may meet some distributed problem which is not easy to observe.

Because of developer cannot debug in production environment, ElasticJob provide `dump` command to export job runtime information for debugging.

For security reason, the information dumped had already mask sensitive information, it instead of real IP address to `ip1`, `ip2` ...  

## Open Listener Port

To open listener port using Java, refer to [Java API job information export configuration](/en/user-manual/elasticjob/configuration/java-api).
To open listener port using Spring Boot Starter, refer to [Spring Boot Starter job information export configuration](/en/user-manual/elasticjob/configuration/java-api).
To open listener port using Spring Namespace, refer to [Spring namespace job information export configuration](/en/user-manual/elasticjob/configuration/spring-namespace).

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
