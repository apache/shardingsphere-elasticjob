+++
pre = "<b>5.3. </b>"
title = "错误处理策略"
weight = 3
+++

错误处理策略，用于作业失败时的处理策略。

| *SPI 名称*            | *详细说明*                      |
| --------------------- | ------------------------------ |
| JobErrorHandler       | 作业执行线程池策略               |

| *已知实现类*           | *详细说明*                      |
| --------------------- | ------------------------------ |
| LogJobErrorHandler    | 记录作业异常日志，但不中断作业执行 |
| DingtalkJobErrorHandler | 记录作业异常日志，但不中断作业执行，并且发送钉钉消息通知 |
| ThrowJobErrorHandler  | 抛出系统异常并中断作业执行        |
| IgnoreJobErrorHandler | 忽略系统异常且不中断作业执行      |
| WechatJobErrorHandler | 记录作业异常日志，但不中断作业执行，并且发送企业微信消息通知 |

