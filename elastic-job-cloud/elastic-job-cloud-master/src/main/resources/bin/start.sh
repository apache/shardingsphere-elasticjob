#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf/*
LIB_DIR=$DEPLOY_DIR/lib/*
CONTAINER_MAIN=com.dangdang.ddframe.job.cloud.Main
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.library.path=/usr/local/lib"

java -classpath $CONF_DIR:$LIB_DIR:. $CONTAINER_MAIN $JAVA_OPTS
