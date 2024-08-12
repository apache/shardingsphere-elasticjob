+++
title = "GraalVM Native Image"
weight = 6
chapter = true
+++

## 背景信息

ElasticJob 已在 GraalVM Native Image 下完成可用性验证。

构建包含 `org.apache.shardingsphere.elasticjob:elasticjob-bootstrap:${elasticjob.version}` 的 Maven 依赖的 GraalVM Native Image，
你需要借助于 GraalVM Native Build Tools。
GraalVM Native Build Tools 提供了 Maven Plugin 和 Gradle Plugin 来简化 GraalVM CE 的 `native-image` 命令行工具的长篇大论的 shell 命令。

ElasticJob 要求在如下或更高版本的 `GraalVM CE` 完成构建 GraalVM Native Image。使用者可通过 `SDKMAN!` 快速切换 JDK。这同理
适用于 https://sdkman.io/jdks#graal ， https://sdkman.io/jdks#nik 和 https://sdkman.io/jdks#mandrel 等 `GraalVM CE` 的下游发行版。

- GraalVM CE For JDK 22.0.2，对应于 SDKMAN! 的 `22.0.2-graalce`

用户依然可以使用 SDKMAN! 上的 `21.0.2-graalce` 等旧版本的 GraalVM CE 来构建 ElasticJob 的 GraalVM Native Image 产物。
ElasticJob 不为已停止维护的 GraalVM CE 版本设置 CI。

## 使用 ElasticJob 的 Java API

### Maven 生态

使用者需要主动使用 GraalVM Reachability Metadata 中央仓库。
如下配置可供参考，以配置项目额外的 Maven Profiles，以 GraalVM Native Build Tools 的文档为准。

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

### Gradle 生态

使用者需要主动使用 GraalVM Reachability Metadata 中央仓库。
如下配置可供参考，以配置项目额外的 Gradle Tasks，以 GraalVM Native Build Tools 的文档为准。
由于 https://github.com/gradle/gradle/issues/17559 的限制，用户需要通过 Maven 依赖的形式引入 Metadata Repository 的 JSON 文件。
参考 https://github.com/graalvm/native-build-tools/issues/572 。

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

## 使用 ElasticJob 的 Spring Boot Starter

### Maven 生态

使用者需要主动使用 GraalVM Reachability Metadata 中央仓库。
如下配置可供参考，以配置项目额外的 Maven Profiles，以 GraalVM Native Build Tools 的文档为准。

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere.elasticjob</groupId>
            <artifactId>elasticjob-spring-boot-starter</artifactId>
            <version>${elasticjob.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
            <version>3.3.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.3.2</version>
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
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>3.3.2</version>
                <executions>
                    <execution>
                        <id>process-test-aot</id>
                        <goals>
                            <goal>process-test-aot</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Gradle 生态

使用者需要主动使用 GraalVM Reachability Metadata 中央仓库。
如下配置可供参考，以配置项目额外的 Gradle Tasks，以 GraalVM Native Build Tools 的文档为准。
由于 https://github.com/gradle/gradle/issues/17559 的限制，用户需要通过 Maven 依赖的形式引入 Metadata Repository 的 JSON 文件。
参考 https://github.com/graalvm/native-build-tools/issues/572 。

```groovy
plugins {
   id 'org.springframework.boot' version '3.3.2'
   id 'io.spring.dependency-management' version '1.1.6'
   id 'org.graalvm.buildtools.native' version '0.10.2'
}

dependencies {
   implementation 'org.springframework.boot:spring-boot-starter-jdbc'
   implementation 'org.springframework.boot:spring-boot-starter-web'
   implementation 'org.apache.shardingsphere.elasticjob:elasticjob-spring-boot-starter:${elasticjob.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.10.2', classifier: 'repository', ext: 'zip')
}

graalvmNative {
   metadataRepository {
        enabled.set(false)
   }
}
```


## 对于 sbt 等不被 GraalVM Native Build Tools 支持的构建工具

此类需求需要在 https://github.com/graalvm/native-build-tools 打开额外的 issue 并提供对应构建工具的 Plugin 实现。

## 使用限制

1. 使用者依然需要在 `src/main/resources/META-INF/native-image` 文件夹或 `src/test/resources/META-INF/native-image` 文件夹配置独立文件的 GraalVM Reachability Metadata。
使用者可通过 GraalVM Native Build Tools 的 GraalVM Tracing Agent 来快速采集 GraalVM Reachability Metadata。

2. 对于在 Linux 系统下执行 `elasticJobType` 为 `SCRIPT` 的 `org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap`，
若 `script.command.line` 设置为构建 GraalVM Native Image 时， 私有项目的 classpath 下的某个 `.sh` 文件在 GraalVM Native Image 下的相对路径，
则此 `.sh` 文件至少提前设置 `rwxr-xr-x` 的 POSIX 文件权限。
因为 `com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystem` 显然不支持 `java.nio.file.attribute.PosixFileAttributeView`。
长话短说，用户应该避免在作业内包含类似如下的逻辑，

```java
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public class ExampleUtils {
    public void setPosixFilePermissions() throws IOException {
        URL resource = ExampleUtils.class.getResource("/script/demo.sh");
        assert resource != null;
        Path path = Paths.get(resource.getPath());
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr-xr-x"));
    }
}
```

3. `企业微信通知策略`，`钉钉通知策略`，`邮件通知策略`尚未在 GraalVM Native Image 下可用。

4. ElasticJob 的 Spring 命名空间集成模块 `org.apache.shardingsphere.elasticjob:elasticjob-spring-namespace` 尚未在 GraalVM Native Image 下可用。

## 贡献 GraalVM Reachability Metadata

ElasticJob 对在 GraalVM Native Image 下的可用性的验证，是通过 GraalVM Native Build Tools 的 Maven Plugin 子项目来完成的。
通过在 JVM 下运行单元测试，为单元测试打上 `junit-platform-unique-ids*` 标签，此后构建为 GraalVM Native Image 进行 nativeTest 来测试
在 GraalVM Native Image 下的单元测试覆盖率。请贡献者不要使用 `io.kotest:kotest-runner-junit5-jvm:5.5.4` 等在 `test listener` mode 下
failed to discover tests 的测试库。

ElasticJob 定义了 `elasticjob-test-native` 的 Maven Module 用于为 native Test 提供小型的单元测试子集，
此单元测试子集避免了使用 Mockito 等 native Test 下无法使用的第三方库。

ElasticJob 定义了 `nativeTestInElasticJob` 的 Maven Profile 用于为 `elasticjob-test-native` 模块执行 nativeTest 。

假设贡献者处于新的 Ubuntu 22.04.4 LTS 实例下，其可通过如下 bash 命令通过 SDKMAN! 管理 JDK 和工具链，
并为 `elasticjob-test-native` 子模块执行 nativeTest。

贡献者必须安装 Docker Engine 以执行 `testcontainers-java` 相关的单元测试。

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

当贡献者发现缺少与 ElasticJob 无关的第三方库的 GraalVM Reachability Metadata 时，应当在
https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue， 并提交包含依赖的第三方库缺失的 GraalVM Reachability
Metadata 的 PR。ElasticJob 在 `elasticjob-reachability-metadata` 子模块主动托管了部分第三方库的 GraalVM Reachability Metadata。

如果 nativeTest 执行失败， 应为单元测试生成初步的 GraalVM Reachability Metadata，
并手动调整 `elasticjob-reachability-metadata` 子模块的 classpath 的 `META-INF/native-image/org.apache.shardingsphere.elasticjob/elasticjob-reachability-metadata/` 文件夹下的内容以修复 nativeTest。
如有需要，请使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 注解或 `org.graalvm.nativeimage.imagecode` 的
System Property 屏蔽部分单元测试在 GraalVM Native Image 下运行。

ElasticJob 定义了 `generateMetadata` 的 Maven Profile 用于在 GraalVM JIT Compiler 下携带 GraalVM Tracing Agent 执行单元测试，
并在 `elasticjob-reachability-metadata` 子模块的 classpath 的 `META-INF/native-image/org.apache.shardingsphere.elasticjob/generated-reachability-metadata/` 文件夹下，
生成或覆盖已有的 GraalVM Reachability Metadata 文件。可通过如下 bash 命令简单处理此流程。
贡献者仍可能需要手动调整具体的 JSON 条目，并适时调整 Maven Profile 和 GraalVM Tracing Agent 的 Filter 链。
针对 `elasticjob-reachability-metadata` 子模块，
手动增删改动的 JSON 条目应位于 `META-INF/native-image/org.apache.shardingsphere.elasticjob/elasticjob-reachability-metadata/` 文件夹下，
而 `META-INF/native-image/org.apache.shardingsphere.elasticjob/generated-reachability-metadata/` 中的条目仅应由 `generateMetadata` 的 Maven Profile 生成。

以下命令仅为 `elasticjob-test-native` 生成 Conditional 形态的 GraalVM Reachability Metadata 的一个举例。
生成的 GraalVM Reachability Metadata 位于 `elasticjob-reachability-metadata` 子模块下。

对于测试类和测试文件独立使用的 GraalVM Reachability Metadata，贡献者应该放置到 `shardingsphere-test-native` 子模块的 classpath 的
`META-INF/native-image/elasticjob-test-native-test-metadata/` 下。

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
```
