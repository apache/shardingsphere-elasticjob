#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
LIB_DIR=${DEPLOY_DIR}/lib/*
CONTAINER_MAIN=com.dangdang.ddframe.job.cloud.scheduler.Bootstrap
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.library.path=/usr/local/lib:/usr/lib:/usr/lib64"

java ${JAVA_OPTS} -classpath ${LIB_DIR}:. ${CONTAINER_MAIN}
