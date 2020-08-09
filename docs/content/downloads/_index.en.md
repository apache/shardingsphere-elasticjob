+++
pre = "<b>6. </b>"
title = "Downloads"
weight = 6
chapter = true
+++

## Latest releases

ElasticJob is released as source code tarballs with corresponding binary tarballs for convenience. 
The downloads are distributed via mirror sites and should be checked for tampering using GPG or SHA-512.

**ElasticJob**

| Version | Release date | Description | Downloads |
| - | - | - | - |
| 3.0.0-alpha | Aug 9, 2020  | Source codes | [[src]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-src.zip) [[asc]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-src.zip.asc) [[sha512]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-src.zip.sha512) |
|             |              | ElasticJob-Lite Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-lite-bin.tar.gz) [[asc]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-lite-bin.tar.gz.asc) [[sha512]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-lite-bin.tar.gz.sha512) |
|             |              | ElasticJob-Cloud-Scheduler Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-cloud-scheduler-bin.tar.gz) [[asc]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-cloud-scheduler-bin.tar.gz.asc) [[sha512]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-cloud-scheduler-bin.tar.gz.sha512) |
|             |              | ElasticJob-Cloud-Executor Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-cloud-executor-bin.tar.gz) [[asc]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-cloud-executor-bin.tar.gz.asc) [[sha512]](https://www.apache.org/dyn/closer.cgi/shardingsphere/elasticjob-3.0.0-alpha/apache-shardingsphere-elasticjob-3.0.0-alpha-cloud-executor-bin.tar.gz.sha512) |

**ElasticJob-UI**

| Version | Release date | Description | Downloads |
| - | - | - | - |
| 3.0.0-alpha | Coming soon  | Source codes | [[src]]() [[asc]]() [[sha512]]() |
|             |              | ElasticJob-Lite-UI Binary Distribution | [[tar]]() [[asc]]() [[sha512]]() |
|             |              | ElasticJob-Cloud-UI Binary Distribution | [[tar]]() [[asc]]() [[sha512]]() |

## All releases

Find all releases in the [Archive repository](https://archive.apache.org/dist/shardingsphere/).

## Verify the releases

[PGP signatures KEYS](https://downloads.apache.org/shardingsphere/KEYS)

It is essential that you verify the integrity of the downloaded files using the PGP or SHA signatures. 
The PGP signatures can be verified using GPG or PGP. Please download the KEYS as well as the asc signature files for relevant distribution. 
It is recommended to get these files from the main distribution directory and not from the mirrors.

```shell
gpg -i KEYS
```

or

```shell
pgpk -a KEYS
```

or

```shell
pgp -ka KEYS
```

To verify the binaries/sources you can download the relevant asc files for it from main distribution directory and follow the below guide.

```shell
gpg --verify apache-shardingsphere-********.asc apache-shardingsphere-elasticjob-*********
```

or

```shell
pgpv apache-shardingsphere-elasticjob-********.asc
```

or

```shell
pgp apache-shardingsphere-elasticjob-********.asc
```
