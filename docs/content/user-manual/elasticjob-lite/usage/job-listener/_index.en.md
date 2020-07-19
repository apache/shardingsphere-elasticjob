+++
title = "Job Listener"
weight = 2
chapter = true
+++

ElasticJob-Lite provides job listeners, which are used to perform monitoring methods before and after task execution.
Listeners are divided into regular listeners executed by each job node and distributed listeners executed by only a single node in a distributed scenario.
This chapter will introduce how to use them in detail.

After the job dependency (DAG) function is developed, the job listener function may be considered to be deleted.