+++
title = "GraalVM Native Image 支持"
weight = 6
chapter = true
+++

## 对 GraalVM Native Image 的支持

ElasticJob Lite 通过 GAV 信息为 `org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata:${project.version}` 
的 Maven 库，提供了 GraalVM Native Image 运行所需要的 GraalVM Reachability Metadata。如果你期望构建包含 ElasticJob Lite 的 
GraalVM Native Image，你可在 `pom.xml` 引入如下依赖。

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

## 内部的 GraalVM Reachability Metadata 条目

`org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata` 托管了如下 Maven 库的 GraalVM Reachability 
Metadata，一旦 https://github.com/oracle/graalvm-reachability-metadata 对如下库的 GraalVM Reachability Metadata 进行了 
Release 流程, 下列第三方库的相关 JSON 文件将在 ElasticJob 一侧被删除。

-  `org.apache.zookeeper:zookeeper:3.9.0`
- `org.apache.curator:curator-client:5.5.0`
- `org.apache.curator:curator-framework:5.5.0`
- `org.apache.curator:curator-framework:5.5.0`
- `org.apache.shardingsphere.elasticjob:elasticjob-lite-core:${project.version}`

用户可以通过在自有项目的资源根目录或测试资源文件夹，通过新建对应库信息的 `/META-INF/native-image/${project.groupId}/${project.artifactId}/${project.version}` 
文件夹来覆写被传入项目的 GraalVM Reachability Metadata 。

即使用户不使用 `org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata` 库，用户依然可以自己编写 GraalVM 
Reachability Metadata 的相关文件来为 ElasticJob Lite 提供 GraalVM Native Image 支持。

## 已知限制

1. 用户无法在 GraalVM Native Image 使用 elasticJobType 为 SCRIPT 的 Job。参考 https://github.com/oracle/graal/issues/7390 。

2. 用户无法在 GraalVM Native Image 下使用与 `org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration` 相关的 Tracing 功能。

3. ElasticJob 的 Spring Boot Starter 尚未在 GraalVM Native Image 下可用。

## 贡献 GraalVM Reachability Metadata

此节文本针对贡献者。假设贡献者位于新的 Ubuntu 22.04.3 实例下，可通过 SDKMAN! 初始化 GraalVM CE 环境。

```bash
sdk install java 17.0.8-graalce
sdk use java 17.0.8-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y
```


对于与 ElasticJob 无关的 GraalVM Reachability Metadata，相关的 issue 和 PR 应当首先创建在 https://github.com/oracle/graalvm-reachability-metadata 。
该存储库使 GraalVM Native Image 的用户能够共享和重用 Java 生态系统中的库和框架的元数据。

`org.apache.shardingsphere.elasticjob:elasticjob-lite-reachability-metadata` 维护了一组单元测试子集，此子集避开对 Mockito 相关的
类的使用，以服务于项目 CI 对 Native Image 的测试。参考 https://github.com/mockito/mockito/issues/2862。

在 ElasticJob 一侧，存在 Maven Profile 为 generateMetadata，用户可在 ElasticJob 项目根目录执行如下命令来采集 GraalVM Reachability Metadata。

```bash
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C -B clean test native:metadata-copy
```

在 ElasticJob 一侧，存在 Maven Profile 为 nativeTestInElasticJob，用户可在 ElasticJob 项目根目录执行如下命令来执行特定于 GraalVM 
Native Build Tools 的 nativeTest，以验证 Native Image 中的单元测试覆盖率。

```bash
./mvnw -PnativeTestInElasticJob -T1C -B -e clean test
```

对于特定于单元测试的 GraalVM Reachability Metadata，请放置在 `elasticjob-lite/elasticjob-lite-reachability-metadata/src/test/resources/META-INF/native-image/${project.artifactId}-test-metadata/` 
文件夹下，`${project.artifactId}` 为对应单元测试涉及的子模块。如有需要，请使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 
注解或 `org.graalvm.nativeimage.imagecode` 的 System Property 屏蔽部分单元测试在 GraalVM Native Image 下运行。
