+++
pre = "<b>5.4. </b>"
title = "Job Class Name Provider"
weight = 4
+++

Job class name provider, used to provide job class name in different contain environments.

| *SPI Name*                      | *Description*                                           |
| ------------------------------- | ------------------------------------------------------- |
| JobClassNameProvider            | Job class name provider                                 |

| *Implementation Class*          | *Description*                                           |
| ------------------------------- | ------------------------------------------------------- |
| DefaultJobClassNameProvider     | Job class name provider in standard environment         |
| SpringProxyJobClassNameProvider | Job class name provider in Spring container environment |
