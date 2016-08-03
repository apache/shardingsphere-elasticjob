#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
LIB_DIR=$DEPLOY_DIR/lib/*
MAIN_CLASS=com.dangdang.ddframe.job.cloud.example.Main
java -classpath $LIB_DIR:. $MAIN_CLASS $1 $2 $3
