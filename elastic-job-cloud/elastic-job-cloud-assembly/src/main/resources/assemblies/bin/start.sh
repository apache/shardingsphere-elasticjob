#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
LIB_DIR=$DEPLOY_DIR/lib/*
CONTAINER_MAIN=com.dangdang.ddframe.job.cloud.AgentMain
java -classpath $LIB_DIR:. $CONTAINER_MAIN $1 $2
