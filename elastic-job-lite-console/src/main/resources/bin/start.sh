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

show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "  -p <port>          Server port (default: 8899)"
    exit 1
}

if [ $# -ne 0 ] && [ $# -ne 2 ]; then
  show_usage
fi

port="8899"

if [ $# -eq 2 ]; then
  while getopts p: arg
  do    case "$arg" in
          p) port="$OPTARG";;
          [?]) show_usage;;
        esac
  done
fi

if [ "$port" = "" ]; then
  show_usage
fi

cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
CLASS_PATH=.:${DEPLOY_DIR}/conf:${DEPLOY_DIR}/lib/*:${DEPLOY_DIR}/ext-lib/*
CONSOLE_MAIN=org.apache.shardingsphere.elasticjob.lite.console.ConsoleBootstrap

exec java -classpath ${CLASS_PATH}:. ${CONSOLE_MAIN} ${port}
