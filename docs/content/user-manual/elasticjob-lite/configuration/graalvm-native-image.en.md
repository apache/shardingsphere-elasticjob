+++
title = "GraalVM Native Image Support"
weight = 6
chapter = true
+++

## Support for GraalVM Native Image

ElasticJob Lite provides the GraalVM Reachability Metadata needed to build GraalVM Native Images. No additional 
processing is required after the introduction of `elasticjob-lite-core` in `pom.xml`.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere.elasticjob</groupId>
            <artifactId>elasticjob-lite-core</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

## Internal GraalVM Reachability Metadata entry

`org.apache.shardingsphere.elasticjob:elasticjob-infra-reachability-metadata` hosts GraalVM Reachability Metadata for 
the following Maven library, once https://github.com/oracle/graalvm-reachability-metadata is done Release process with 
the GraalVM Reachability Metadata for the following libraries, the relevant JSON files for the following third-party 
libraries will be deleted on the ElasticJob side.

- `org.apache.zookeeper:zookeeper:3.9.0`

Users can create a new corresponding library folder called `/META-INF/native-image/${project.groupId}/${project.artifactId}/${project.version}` 
in the resource root directory of their own project or test resource folder to override the GraalVM Reachability 
Metadata of the incoming project.

Users can still write their own GraalVM Reachability Metadata files to provide GraalVM Native Image support for 
libraries that lack GraalVM Reachability Metadata.

## Known limitations

1. Users cannot use jobs with `elasticJobType` as `SCRIPT` in GraalVM Native Image. Refer to https://github.com/oracle/graal/issues/7390 .

2. Users cannot use the tracing function related to `org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration`
under GraalVM Native Image.

3. ElasticJob's Spring Boot Starter is not yet available under GraalVM Native Image.

## Contribute GraalVM Reachability Metadata

This section text is for contributors. Assuming the contributor is under a new Ubuntu 22.04.3 instance, the `GraalVM CE`
environment can be initialized via `SDKMAN!`.

```bash
sdk install java 21-graalce
sdk use java 21-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y
```

For GraalVM Reachability Metadata that is not related to ElasticJob, issues and PRs should first be created in the https://github.com/oracle/graalvm-reachability-metadata .
The repository enables users of GraalVM Native Image to share and reuse metadata for libraries and frameworks in the 
Java ecosystem.

ElasticJob maintains a subset of unit tests that eschews the use of Mockito-related classes to serve the project CI's 
testing of Native Images. Refer to https://github.com/mockito/mockito/issues/2862 .

- `org.apache.shardingsphere.elasticjob.lite.fixture.reachability.metadata.**`

On the ElasticJob side, there is a Maven Profile as `generateMetadata`, and users can execute the following command in 
the root directory of the ElasticJob project to capture GraalVM Reachability Metadata. Manually delete the JSON file 
without any specific entries.

```bash
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
```

On the ElasticJob side, there is a Maven Profile as `nativeTestInElasticJob`, and users can execute the following 
command at the root of the ElasticJob project to execute GraalVM-specific nativeTest of Native Build Tools to verify 
unit test coverage in GraalVM Native Image.

```bash
./mvnw -PnativeTestInElasticJob -T1C -e clean test
```

For GraalVM Reachability Metadata specific to unit tests, be placed in `${project.basedir}/src/test/resources/META-INF/native-image/${project.artifactId}-test-metadata/`
folder, `${project.basedir}` and `${project.artifactId}` are submodules involved in the corresponding unit test. If 
needed, use `org.junit.jupiter.api.condition.DisabledInNativeImage` annotations or `org.graalvm.nativeimage.imagecode`
System Property masked unit tests to run under GraalVM Native Image.
