#!/usr/bin/env bash

# 使用dev环境的container环境构建，这样主要是方便测试container的逻辑
now_dir=`pwd`
cd `dirname $0`
res_dir=`pwd`/res


builder_jar=${res_dir}/ratel-keygen-1.0.0.jar

if [ -f ${builder_jar} ] ;then
    echo "use ${builder_jar}"
else
    echo "can not find ratel-keygen build jar in path:${builder_jar}"
    echo -1
fi

cd ${now_dir}

java -jar ${builder_jar} $*
