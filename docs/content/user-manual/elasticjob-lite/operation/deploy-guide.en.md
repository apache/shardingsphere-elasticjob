+++
title = "Deploy Guide"
weight = 1
chapter = true
+++

## Application deployment

1. Start the ZooKeeper of the ElasticJob-Lite designated registry.
1. Run the jar file containing ElasticJob-Lite and business code. It is not limited to the startup mode of jar or war.
1. When the job server is configured with multiple network cards, the network card address can be specified by setting the system variable `elasticjob.preferred.network.interface`. ElasticJob obtains the first non-loopback available IPV4 address in the network card list by default.

## Operation and maintenance platform and RESTFul API deployment (optional)

1. Unzip `elasticjob-lite-console-${version}.tar.gz` and execute `bin\start.sh`.
1. Open the browser and visit `http://localhost:8899/` to access the console. 8899 is the default port number. You can customize the port number by entering `-p` through the startup script.
1. The method of accessing RESTFul API is the same as the console.
1. `elasticjob-lite-console-${version}.tar.gz` can be obtained by compiling `mvn install`.

