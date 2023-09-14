+++
title = "GraalVM Native Image Support"
weight = 6
chapter = true
+++

## Support for GraalVM Native Image

ElasticJob Lite provides the GraalVM Reachability Metadata required for GraalVM Native Image to run through the Maven 
library whose GAV information is `org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata:${project.version}`. 
If you want to build the GraalVM Native Image that includes ElasticJob Lite, you can introduce the following 
dependencies in `pom.xml`.

```xml
<project>
     <dependencies>
         <dependency>
             <groupId>org.apache.shardingsphere.elasticjob</groupId>
             <artifactId>elasticjob-lite-core</artifactId>
             <version>${project.version}</version>
         </dependency>
         <dependency>
             <groupId>org.apache.shardingsphere.elasticjob</groupId>
             <artifactId>elasticjob-lite-reachability-metadata</artifactId>
             <version>${project.version}</version>
         </dependency>
     </dependencies>
</project>
```

## Internal GraalVM Reachability Metadata entries

`org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata` hosts the GraalVM Reachability Metadata of 
the following Maven library. Once https://github.com/oracle/graalvm-reachability-metadata is used for the GraalVM 
Reachability Metadata of the following library After the Release process, the JSON files related to the following 
third-party libraries will be deleted on the ElasticJob side.

- `org.apache.zookeeper:zookeeper:3.9.0`
- `org.apache.curator:curator-client:5.5.0`
- `org.apache.curator:curator-framework:5.5.0`
- `org.apache.curator:curator-framework:5.5.0`
- `org.apache.shardingsphere.elasticjob:elasticjob-lite-core:${project.version}`

Users can create a new `/META-INF/native-image/${project.groupId}/${project.artifactId}/${project.version}` folder 
corresponding to the library information in the resource root directory of their own project or the test resource folder 
to overwrite the GraalVM Reachability Metadata passed into the project.

Even if users do not use the `org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata` library, 
users can still write their own GraalVM Reachability Metadata related files to provide GraalVM Native Image support for 
ElasticJob Lite.

## Known limitations

1. Users cannot use jobs whose elasticJobType is SCRIPT in GraalVM Native Image. Reference https://github.com/oracle/graal/issues/7390.

2. Users cannot use Tracing functions related to `org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration`
under GraalVM Native Image.

3. ElasticJob’s Spring Boot Starter is not yet available under GraalVM Native Image.

## Contribute GraalVM Reachability Metadata

The text in this section is directed to contributors. Assuming the contributor is under a new Ubuntu 22.04.3 instance, 
the GraalVM CE environment can be initialized via `SDKMAN!`.

```bash
sdk install java 17.0.8-graalce
sdk use java 17.0.8-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y
```

For GraalVM Reachability Metadata not related to ElasticJob, related issues and PRs should first be created at 
https://github.com/oracle/graalvm-reachability-metadata. This repository enables users of GraalVM Native Image to share 
and reuse metadata for libraries and frameworks across the Java ecosystem.

`org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata` maintains a subset of unit tests that 
avoids the use of Mockito-related classes to serve the project CI's testing of Native Image. 
Reference https://github.com/mockito/mockito/issues/2862 .

On the ElasticJob side, there is a Maven Profile called `generateMetadata`. Users can execute the following command in 
the ElasticJob project root directory to collect GraalVM Reachability Metadata.

```bash
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C -B clean test native:metadata-copy
```

On the ElasticJob side, the Maven Profile exists as `nativeTestInElasticJob`. Users can execute the following command in
the ElasticJob project root directory to execute nativeTest specific to GraalVM Native Build Tools to verify the unit 
test coverage in Native Image.

```bash
./mvnw -PnativeTestInElasticJob -T1C -B -e clean test
```

For unit test-specific GraalVM Reachability Metadata, place it in `elasticjob-lite/elasticjob-lite-reachability-metadata/src/test/resources/META-INF/native-image/${project.artifactId}-test-metadata/` 
folder, `${project.artifactId}` is the submodule involved in the corresponding unit test. If necessary, use the 
`org.junit.jupiter.api.condition.DisabledInNativeImage` annotation or the `org.graalvm.nativeimage.imagecode` System 
Property blocks some unit tests from running under GraalVM Native Image.
