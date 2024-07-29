+++
title = "GraalVM Native Image"
weight = 6
chapter = true
+++

## Background information

ElasticJob has been verified for availability under GraalVM Native Image.

To build a GraalVM Native Image with the Maven dependency `org.apache.shardingsphere.elasticjob:elasticjob-bootstrap:${elasticjob.version}`,
you need to use GraalVM Native Build Tools.

GraalVM Native Build Tools provides Maven Plugin and Gradle Plugin to simplify the long-winded shell commands of GraalVM CE's `native-image` command line tool.

ElasticJob requires the following or higher versions of `GraalVM CE` to build the GraalVM Native Image. Users can quickly switch JDKs through `SDKMAN!`. 
This also applies to downstream distributions of `GraalVM CE` such as https://sdkman.io/jdks#graal, https://sdkman.io/jdks#nik and https://sdkman.io/jdks#mandrel.

- GraalVM CE For JDK 22.0.2, corresponding to `22.0.2-graalce` of SDKMAN!

Users can still use old versions of GraalVM CE such as `21.0.2-graalce` on SDKMAN! to build ElasticJob's GraalVM Native Image product.
ElasticJob does not set CI for GraalVM CE versions that have stopped maintenance.

## Using ElasticJob's Java API

### Maven Ecosystem

Users need to actively use the GraalVM Reachability Metadata Central Repository.
The following configuration is for reference. To configure additional Maven Profiles for the project, 
refer to the documentation of GraalVM Native Build Tools.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere.elasticjob</groupId>
            <artifactId>elasticjob-bootstrap</artifactId>
            <version>${elasticjob.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.buildtools</groupId>
                <artifactId>native-maven-plugin</artifactId>
                <version>0.10.2</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <id>build-native</id>
                        <goals>
                            <goal>compile-no-fork</goal>
                        </goals>
                        <phase>package</phase>
                    </execution>
                    <execution>
                        <id>test-native</id>
                        <goals>
                            <goal>test</goal>
                        </goals>
                        <phase>test</phase>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Gradle Ecosystem

Users need to actively use the GraalVM Reachability Metadata Central Repository.
The following configuration is for reference. To configure additional Gradle Tasks for the project, refer to the documentation of GraalVM Native Build Tools.
Due to the limitations of https://github.com/gradle/gradle/issues/17559, users need to introduce the Metadata Repository JSON file in the form of Maven dependencies.
Refer to https://github.com/graalvm/native-build-tools/issues/572.

```groovy
plugins {
   id 'org.graalvm.buildtools.native' version '0.10.2'
}

dependencies {
   implementation 'org.apache.shardingsphere.elasticjob:elasticjob-bootstrap:${elasticjob.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.10.2', classifier: 'repository', ext: 'zip')
}

graalvmNative {
   metadataRepository {
        enabled.set(false)
   }
}
```

## For build tools such as sbt that are not supported by GraalVM Native Build Tools

Such requirements require opening additional issues at https://github.com/graalvm/native-build-tools and providing plugin implementations for the corresponding build tools.

## Usage restrictions

1. Users still need to configure GraalVM Reachability Metadata in separate files in the `src/main/resources/META-INF/native-image` folder or the `src/test/resources/META-INF/native-image` folder.
Users can quickly collect GraalVM Reachability Metadata through the GraalVM Tracing Agent of GraalVM Native Build Tools.

2. For `org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap` with `elasticJobType` as `SCRIPT` under Linux, 
if `script.command.line` is set to the relative path of a `.sh` file in the private project's classpath under the GraalVM Native Image when building the GraalVM Native Image, 
then the `.sh` file must at least have the POSIX file permission of `rwxr-xr-x` set in advance. 
This is because `com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystem` obviously does not support `java.nio.file.attribute.PosixFileAttributeView`.

3. The Spring namespace integration module `org.apache.shardingsphere.elasticjob:elasticjob-spring-namespace` of ElasticJob is not yet available under GraalVM Native Image.

4. The Spring Boot Starter integration module `org.apache.shardingsphere.elasticjob:elasticjob-spring-boot-starter` for ElasticJob is not yet available under GraalVM Native Image.

## Contribute GraalVM Reachability Metadata

ElasticJob's usability verification under GraalVM Native Image is done by the Maven Plugin subproject of GraalVM Native Build Tools.

Unit test coverage under GraalVM Native Image is tested by running unit tests under JVM, 
tagging unit tests with `junit-platform-unique-ids*`, and then building GraalVM Native Image for nativeTest. 
Contributors are requested not to use test libraries such as `io.kotest:kotest-runner-junit5-jvm:5.5.4` that failed to discover tests in `test listener` mode.

ElasticJob defines the `elasticjob-test-native` Maven Module to provide a small subset of unit tests for native Test,
which avoids the use of third-party libraries such as Mockito that cannot be used under native Test.

ElasticJob defines the `nativeTestInElasticJob` Maven profile to execute nativeTest for the `elasticjob-test-native` module.

Assuming the contributor is on a fresh Ubuntu 22.04.4 LTS instance, he can use SDKMAN! to manage JDK and toolchains with the following bash command,
and execute nativeTest for the `elasticjob-test-native` submodule.

Contributors must install Docker Engine to execute the `testcontainers-java` related unit tests.

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.0.2-graalce
sdk use java 22.0.2-graalce
sudo apt-get install build-essential zlib1g-dev -y

git clone git@github.com:apache/shardingsphere-elasticjob.git
cd ./shardingsphere-elasticjob/
./mvnw -PnativeTestInElasticJob -T1C -e clean test
```

When contributors find that GraalVM Reachability Metadata for third-party libraries not related to ElasticJob is missing, 
they should open a new issue at https://github.com/oracle/graalvm-reachability-metadata, 
and submit a PR with missing GraalVM Reachability Metadata for dependent third-party libraries. 
ElasticJob proactively hosts GraalVM Reachability Metadata for some third-party libraries in the `elasticjob-reachability-metadata` submodule.

If nativeTest fails, generate preliminary GraalVM Reachability Metadata for unit tests, 
and manually adjust the contents of the `META-INF/native-image/org.apache.shardingsphere.elasticjob/elasticjob-reachability-metadata/` folder in the classpath of the `elasticjob-reachability-metadata` submodule to fix nativeTest.
If necessary, 
use the `org.junit.jupiter.api.condition.DisabledInNativeImage` annotation or the `org.graalvm.nativeimage.imagecode` System Property to shield some unit tests from running under the GraalVM Native Image.

ElasticJob defines the `generateMetadata` Maven Profile to execute unit tests with the GraalVM Tracing Agent under the GraalVM JIT Compiler,
and generates or overwrites the existing GraalVM Reachability Metadata file in the `META-INF/native-image/org.apache.shardingsphere.elasticjob/generated-reachability-metadata/` folder in the classpath of the `elasticjob-reachability-metadata` submodule. 
This process can be easily handled by the following bash command. 
Contributors may still need to manually adjust specific JSON entries and adjust the filter chain of the Maven Profile and GraalVM Tracing Agent as appropriate. 
For the `elasticjob-reachability-metadata` submodule,
manually added, deleted, and modified JSON entries should be located in the `META-INF/native-image/org.apache.shardingsphere.elasticjob/elasticjob-reachability-metadata/` folder,
while the entries in `META-INF/native-image/org.apache.shardingsphere.elasticjob/generated-reachability-metadata/` should only be generated by the `generateMetadata` Maven Profile.

The following command is just an example of generating Conditional GraalVM Reachability Metadata for `elasticjob-test-native`.
The generated GraalVM Reachability Metadata is located in the `elasticjob-reachability-metadata` submodule.

For GraalVM Reachability Metadata used independently by test classes and test files, 
contributors should place it in the classpath of the shardingsphere-test-native submodule under `META-INF/native-image/elasticjob-test-native-test-metadata/`.

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
```
