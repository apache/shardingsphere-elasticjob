+++
pre = "<b>3.6. </b>"
title = "Job Open Ecosystem"
weight = 6
chapter = true
+++

Flexible customized jobs is the most important design change in ElasticJob 3.x .
The new version is based on the design concept of the Apache ShardingSphere pluggable architecture, and the new Job API was created.
It is intended to enable developers to expand the types of jobs in a more convenient and isolated way, and create an ecosystem of ElasticJob jobs.

While ElasticJob provides functions such as elastic scaling and distributed management of jobs, it does not limit the types of jobs.
It uses flexible job APIs to decouple jobs into job interfaces and actuator interfaces.
Users can customize new job types, such as script execution, HTTP service execution, big data jobs, file jobs, etc.
At present, ElasticJob has built-in simple jobs, data flow jobs, and script execution jobs, and has completely opened up the extension interface. Developers can introduce new job types through SPI, and they can easily give back to the community.

## Job interface

ElasticJob jobs can be divided into two types: `Class-based Jobs` and `Type-based Jobs`.

`Class-based Jobs` are directly used by developers, who need to implement the job interface to realize business logic. Typical representatives: Simple type, Dataflow type.
`Type-based Jobs` only need to provide the type name, developers do not need to implement the job interface, but use it through external configuration. Typical representatives: Script type, HTTP type (Since 3.0.0-beta).

## Actuator interface

It is used to execute user-defined job interfaces and weave into the ElasticJob ecosystem through Java's SPI mechanism.
