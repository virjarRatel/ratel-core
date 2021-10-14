#!/usr/bin/env bash

##
## base common 项目，用来实现跨环境变量定义
## 实现gradle脚本和java library 和Android app中同时依赖
##

source /etc/profile

cd `dirname $0`
cd ../..



if [ $? != 0 ] ;then
    echo "base-lib-allcommon jar assemble failed"
    exit $?
fi
./gradlew base-lib-allcommon:assemble


out_jar_path="base-lib-allcommon/build/libs/base-lib-allcommon.jar"

if [ ! -n ${out_jar_path} ] ; then
    echo "can not find output jar ${out_jar_path}"
    exit -1
fi

cp ${out_jar_path} base-lib-allcommon-lib/

./gradlew  base-lib-allcommon:sourcesJar
cp base-lib-allcommon/build/libs/base-lib-allcommon-sources.jar base-lib-allcommon-lib/

echo "base-common api install success"