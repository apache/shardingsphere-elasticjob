#!/bin/bash
java -classpath lib/*:. -Djava.library.path=/usr/local/lib:/usr/lib:/usr/lib64 com.dangdang.ddframe.job.example.CloudJobMain $1
