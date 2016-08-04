+++
date = "2016-01-27T16:14:21+08:00"
title = "1.x 阅读源码编译问题说明"
weight=1009
+++

# 阅读源码编译问题说明

因为关注极简代码，`Elastic-Job`使用`lombok`。在阅读源码的过程中会遇到`@Getter, @Setter`等注解导致不能编译，请按照以下步骤安装`lombok`到你的`IDE`，只使用发布包请不用关注。

## lombok安装指南

### Eclipse

首先配置好`Eclipse`环境，然后双击打开[lombok.jar](https://projectlombok.org/downloads/lombok.jar)文件。

![lombok-eclipse](../../../img/1.x/lombok-eclipse.jpg)

确认`Eclipse`的安装路径，点击`install/update`按钮，即可完成安装，最后需要重启`Eclipse`。

** 如何确认安装成功？**

确认`Eclipse`安装路径下有`lombok.jar`包，并且配置文件`eclipse.ini`中是否已添加如下内容：

```
-javaagent:lombok.jar
-Xbootclasspath/a:lombok.jar
```

否则请自行将缺少的部分添加到相应的位置即可。

安装完成后可直接使用元注解简化你的POJO。如：

![lombok-pojo](../../../img/1.x/lombok-pojo.jpg)

### 其它IDE

请参照[`lombok`官方](https://projectlombok.org/download.html)提供的解决方案。
`Lombok`还可以提供很多其他功能，如`log`变量自动生成等，可参阅`lombok`官网。
