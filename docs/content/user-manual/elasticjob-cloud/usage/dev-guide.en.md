+++
title = "Dev Guide"
weight = 1
chapter = true
+++

## Job development

ElasticJob-Lite and ElasticJob-Cloud provide a unified job interface, developers only need to develop business jobs once, and then they can deploy to different environments according to different configurations.

For details of job development, please refer to [ElasticJob-Lite user manual](/en/user-manual/elasticjob-lite/usage/).

## Job start

You need to define the `main` method and call it `JobBootstrap.execute()`, for example:

```java
public class MyJobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute(new MyJob());
    }
}
```
