#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=${DEPLOY_DIR}/conf
LIB_DIR=${DEPLOY_DIR}/lib/*
CONTAINER_MAIN=org.apache.shardingsphere.elasticjob.cloud.scheduler.Bootstrap
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.library.path=/usr/local/lib:/usr/lib:/usr/lib64"

source ${CONF_DIR}/elasticjob-cloud-scheduler.properties
if [ ${hostname} = "" ] || [ ${hostname} = "127.0.0.1" ] || [ ${hostname} = "localhost" ]; then
  echo "Please config hostname in conf/elasticjob-cloud-scheduler.properties with a routable IP address."
  exit;
fi
export LIBPROCESS_IP=${hostname}

java ${JAVA_OPTS} -classpath ${CONF_DIR}/*:${LIB_DIR}:. ${CONTAINER_MAIN}
