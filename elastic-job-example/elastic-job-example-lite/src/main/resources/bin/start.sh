#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=${DEPLOY_DIR}/conf
LIB_DIR=${DEPLOY_DIR}/lib/*
MAIN_CLASS=com.dangdang.ddframe.job.example.SpringLiteJobMain

java -classpath ${CONF_DIR}/*:${LIB_DIR}:. ${MAIN_CLASS}
