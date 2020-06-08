+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "目录结构说明"
weight = 2
prev = "/03-design"
next = "/03-design/lite-design/"
+++

```
elastic-job
    ├──elastic-job-lite                                 lite父模块，不应直接使用
    ├      ├──elastic-job-lite-core                     Java支持模块，可直接使用
    ├      ├──elastic-job-lite-spring                   Spring命名空间支持模块，可直接使用
    ├      ├──elastic-job-lite-lifecyle                 lite作业相关操作模块，不可直接使用
    ├      ├──elastic-job-lite-console                  lite界面模块，可直接使用
    ├──elastic-job-example                              使用示例
    ├      ├──elastic-job-example-embed-zk              供示例使用的内嵌ZK模块
    ├      ├──elastic-job-example-jobs                  作业示例
    ├      ├──elastic-job-example-lite-java             基于Java的使用示例
    ├      ├──elastic-job-example-lite-spring           基于Spring的使用示例
    ├      ├──elastic-job-example-lite-springboot       基于SpringBoot的使用示例
    ├──elastic-job-doc                                  markdown生成文档的项目，使用方无需关注
    ├      ├──elastic-job-lite-doc                      lite相关文档
```
