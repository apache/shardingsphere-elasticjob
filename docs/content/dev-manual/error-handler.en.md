+++
pre = "<b>5.3. </b>"
title = "Error Handler"
weight = 3
+++

Error handler strategy, used to handle error when exception occur during job execution.

| *SPI Name*               | *Description*                                               |
| ------------------------ | ----------------------------------------------------------- |
| JobErrorHandler          | Job error handler                                           |

| *Implementation Class*   | *Description*                                               |
| ------------------------ | ----------------------------------------------------------- |
| LogJobErrorHandler       | Log error and do not interrupt job                          |
| ThrowJobErrorHandler     | Throw system exception and interrupt job                    |
| IgnoreJobErrorHandler    | Ignore exception and do not interrupt job                   |
| EmailJobErrorHandler     | Send email message notification and do not interrupt job    |
| WechatJobErrorHandler    | Send wechat message notification and do not interrupt job   |
| DingtalkJobErrorHandler  | Send dingtalk message notification and do not interrupt job |
