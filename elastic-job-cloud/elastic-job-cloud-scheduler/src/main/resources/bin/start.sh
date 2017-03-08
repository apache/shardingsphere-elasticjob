#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=${DEPLOY_DIR}/conf
LIB_DIR=${DEPLOY_DIR}/lib/*
CONTAINER_MAIN=com.dangdang.ddframe.job.cloud.scheduler.Bootstrap
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.library.path=/usr/local/lib:/usr/lib:/usr/lib64"

source ${CONF_DIR}/elastic-job-cloud-scheduler.properties
if [ ${hostname} = "" ] || [ ${hostname} = "127.0.0.1" ] || [ ${hostname} = "localhost" ]; then
  echo "Please config hostname in conf/elastic-job-cloud-scheduler.properties with a routable IP address."
  exit;
fi
export LIBPROCESS_IP=${hostname}

java ${JAVA_OPTS} -classpath ${CONF_DIR}/*:${LIB_DIR}:. ${CONTAINER_MAIN}
