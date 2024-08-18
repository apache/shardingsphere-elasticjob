+++
title = "Job Sharding Strategy"
weight = 1
+++

## Average Allocation Strategy

Type: AVG_ALLOCATION

Sharding or average by sharding item.

If the job server number and sharding count cannot be divided, 
the redundant sharding item that cannot be divided will be added to the server with small sequence number in turn.
 
For example: 
1. If there are 3 job servers and the total sharding count is 9, each job server is divided into: 1=[0,1,2], 2=[3,4,5], 3=[6,7,8];
2. If there are 3 job servers and the total sharding count is 8, each job server is divided into: 1=[0,1,6], 2=[2,3,7], 3=[4,5];
3. If there are 3 job servers and the total sharding count is 10, each job server is divided into: 1=[0,1,2,9], 2=[3,4,5], 3=[6,7,8].

## Odevity Strategy

Type: ODEVITY

Sharding for hash with job name to determine IP asc or desc.

IP address asc if job name' hashcode is odd;
IP address desc if job name' hashcode is even.
Used to average assign to job server.
 
For example: 
1. If there are 3 job servers with 2 sharding item, and the hash value of job name is odd, then each server is divided into: 1 = [0], 2 = [1], 3 = [];
2. If there are 3 job servers with 2 sharding item, and the hash value of job name is even, then each server is divided into: 3 = [0], 2 = [1], 1 = [].

## Round Robin Strategy

Type: ROUND_ROBIN

Sharding for round robin by name job.
