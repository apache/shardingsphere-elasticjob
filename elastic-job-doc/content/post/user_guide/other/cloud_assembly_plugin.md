+++
date = "2016-07-15T00:50:50+08:00"
title = "Elastic-Job-Cloud打包插件"
weight=26
+++

# Elastic-Job-Cloud打包插件

`Elastic-Job-Cloud`需要将开发者的应用程序以固定格式上传至`Elastic-Job-Cloud-Master`服务器，然后由`Mesos`统一负责分发和运行。

`elastic-job-cloud-assembly`是专门为`Elastic-Job-Cloud`定制的`Maven`打包插件，将应用程序、启动脚本、lib依赖等统一处理，最终生成标准化`tar.gz`包。

## 使用步骤

### 1. 引用打包插件

在应用程序的`pom.xml`中引用打包插件。

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.dangdang</groupId>
                <artifactId>elastic-job-cloud-assembly</artifactId>
                <version>${latest.release.version}</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>assembly</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

### 2. 配置作业

在应用程序的`src\main\resources\conf\`中创建`job.properties`文件，并配置作业。`key`是`job.classes`，`value`为实现`ElasticJob`作业接口的作业类全名称，多个类以`,`分隔。

例:

```
job.classes=xxx.FooJob, xxx.BarJob
```

### 3. 打包插件其他配置

`elastic-job-cloud-assembly`扩展自`maven-assembly`，可使用`maven-assembly-plugin`的全部功能，比如可配置`finalName`修改`tar.gz`文件名。相关信息请参考[官方文档](http://maven.apache.org/plugins/maven-assembly-plugin/)。

但由于标准化需要，`elastic-job-cloud-assembly`不允许开发者自定义`descriptor`(即不允许自定义打包描述文件)。

### 4. 打包

执行`mvn install`命令，结束后即可在`target`路径中找到打包文件。
