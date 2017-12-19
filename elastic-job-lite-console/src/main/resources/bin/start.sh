#!/bin/bash

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
LIB_DIR=${DEPLOY_DIR}/lib/*
CONSOLE_MAIN=io.elasticjob.lite.console.ConsoleBootstrap

java -classpath ${LIB_DIR}:. ${CONSOLE_MAIN} $port
