+++
title = "Console"
weight = 3
chapter = true
+++

The operation and maintenance platform is embedded in the jar package of elasticjob-cloud-scheduler, and there is no need to start an additional WEB server.
The startup port can be adjusted by modifying the http_port parameter in the configuration file. The default port is 8899 and the access address is `http://{your_scheduler_ip}:8899`.

## Log in

Two types of accounts are provided, administrator and guest. The administrator has all operation permissions, and the visitor only has viewing permissions.
The default administrator user name and password are root/root, and the guest user name and password are guest/guest. You can modify the administrator and guest user names and passwords through `conf\auth.properties`.

## Function list

- Application management (publish, modify, view)
- Job management (register, modify, view and delete)
- View job status (waiting to run, running, pending failover)
- Job history view (running track, execution status, historical dashboard)

## Design concept

The operation and maintenance platform uses pure static HTML + JavaScript to interact with the back-end RESTful API. It displays the job configuration and status by reading the job registry, the database displays the job running track and execution status, or updates the job registry data to modify the job configuration.