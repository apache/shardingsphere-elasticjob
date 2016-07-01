#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
SERVER_NAME=elastic-job-example

#LOG LOCATION
LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
PID=`ps -ef | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
if [ -n "$PID" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PID"
    exit 1
fi

CONF_DIR=$DEPLOY_DIR/conf/*
LIB_DIR=$DEPLOY_DIR/lib/*
CONTAINER_MAIN=com.dangdang.ddframe.job.cloud.boot.Bootstrap
java -classpath $CONF_DIR:$LIB_DIR:. $CONTAINER_MAIN $1