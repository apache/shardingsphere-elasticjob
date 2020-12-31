+++
pre = "<b>6. </b>"
title = "下载"
weight = 6
chapter = true

extracss = true

+++

## 最新版本

ElasticJob 的发布版包括源码包及其对应的二进制包。
由于下载内容分布在镜像服务器上，所以下载后应该进行 GPG 或 SHA-512 校验，以此来保证内容没有被篡改。

##### ElasticJob - 版本: 3.0.0-RC1 ( 发布日期: Dec 25, 2020 )

- 源码: [ [SRC](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-src.zip) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-src.zip.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-src.zip.sha512) ]
- ElasticJob-Lite 二进制包: [ [TAR](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-lite-bin.tar.gz) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-lite-bin.tar.gz.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-lite-bin.tar.gz.sha512) ]
- ElasticJob-Cloud-Scheduler 二进制包: [ [TAR](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-scheduler-bin.tar.gz) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-scheduler-bin.tar.gz.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-scheduler-bin.tar.gz.sha512) ]
- ElasticJob-Cloud-Executor 二进制包: [ [TAR](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-executor-bin.tar.gz) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-executor-bin.tar.gz.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-executor-bin.tar.gz.sha512) ]

##### ElasticJob-UI - 版本: 3.0.0-RC1 ( 发布日期: Dec 31, 2020 )

- 源码: [ [SRC](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-ui-src.zip) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-ui-src.zip.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-ui-src.zip.sha512) ]
- ElasticJob-Lite-UI 二进制包: [ [TAR](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-lite-ui-bin.tar.gz) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-lite-ui-bin.tar.gz.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-lite-ui-bin.tar.gz.sha512) ]
- ElasticJob-Cloud-UI 二进制包: [ [TAR](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-ui-bin.tar.gz) ] [ [ASC](https://downloads.apache.org/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-ui-bin.tar.gz.asc) ] [ [SHA512](https://downloads.apache.org/shardingsphere/elasticjob-ui-3.0.0-RC1/apache-shardingsphere-elasticjob-3.0.0-RC1-cloud-ui-bin.tar.gz.sha512) ]

即将发布

## 全部版本

全部版本请到 [Archive repository](https://archive.apache.org/dist/shardingsphere/) 查看。

## 校验版本

[PGP签名文件](https://downloads.apache.org/shardingsphere/KEYS)

使用 PGP 或 SHA 签名验证下载文件的完整性至关重要。
可以使用 GPG 或 PGP 验证 PGP 签名。
请下载 KEYS 以及发布的 asc 签名文件。
建议从主发布目录而不是镜像中获取这些文件。

```shell
gpg -i KEYS
```

或者

```shell
pgpk -a KEYS
```

或者

```shell
pgp -ka KEYS
```

要验证二进制文件或源代码，您可以从主发布目录下载相关的 asc 文件，并按照以下指南进行操作。

```shell
gpg --verify apache-shardingsphere-elasticjob-********.asc apache-shardingsphere-elasticjob-*********
```

或者

```shell
pgpv apache-shardingsphere-elasticjob-********.asc
```

或者

```shell
pgp apache-shardingsphere-elasticjob-********.asc
```
