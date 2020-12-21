+++
title = "Console"
weight = 4
chapter = true
+++

Unzip `elasticjob-lite-console-${version}.tar.gz` and execute `bin\start.sh`.
Open the browser and visit `http://localhost:8899/` to access the console.
8899 is the default port number. You can customize the port number by entering `-p` through the startup script.

## Log in

The console provides two types of accounts: administrator and guest.
The administrator has all operation rights, and the visitors only have the viewing rights.
The default administrator user name and password are root/root，and the guest user name and password are guest/guest，You can modify the administrator and guest user names and passwords through `conf\application.properties`.
```
auth.root_username=root
auth.root_password=root
auth.guest_username=guest
auth.guest_password=guest
```

## Function list

- Login security control
- Registration center, event tracking data source management
- Quickly modify job settings
- View job and server dimension status
- Operational job disable/enable, stop and delete life cycle
- Event tracking query

## Design concept

The operation and maintenance platform has no direct relationship with ElasticJob-Lite. It displays the job status by reading the job registration center data, or updating the registration center data to modify the global configuration.

The console can only control whether the job itself is running, but it cannot control the start of the job process, because the console and the job server are completely separated, and the console cannot control the job server.

## Unsupported item

* Add assignment

The job will be automatically added the first time it runs.
ElasticJob-Lite is started as a jar and has no job distribution function.
To publish jobs entirely through the operation and maintenance platform, please use ElasticJob-Cloud.
