+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "目录结构说明"
weight = 2
prev = "/03-design"
next = "/03-design/roadmap/"
+++

```
elastic-job
    ├──elastic-job-cloud                                cloud父模块，不应直接使用
    ├      ├──elastic-job-cloud-executor                执行器模块，开发作业时需依赖该模块，可直接使用
    ├      ├──elastic-job-cloud-scheduler               调度器模块，可直接使用
    ├──elastic-job-example                              使用示例
    ├      ├──elastic-job-example-embed-zk              供示例使用的内嵌ZK模块
    ├      ├──elastic-job-example-jobs                  作业示例
    ├      ├──elastic-job-example-cloud                 基于Spring的使用示例
    ├──elastic-job-doc                                  markdown生成文档的项目，使用方无需关注
    ├      ├──elastic-job-cloud-doc                      cloud相关文档
```
