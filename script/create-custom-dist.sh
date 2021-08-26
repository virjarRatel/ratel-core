#!/usr/bin/env bash

# 构建一个引擎包，引擎不包含keygen模块，主要分发给企业用户

now_dir=`pwd`
cd `dirname $0`
script_dir=`pwd`
cd ..
#./gradlew container-builder-repkg:assemble
#if [ $? != 0 ] ;then
#    echo "builder jar assemble failed"
#    exit $?
#fi

./script/prod.sh

./gradlew container-builder-repkg:assemble
if [ $? != 0 ] ;then
    echo "builder jar assemble failed"
    exit $?
fi
./script/dev.sh


engineVersionCode=`cat build.gradle | grep ratelEngineVersion | grep -v ratelEngineVersionCode | awk '{print $3}' | awk -F "\"" '{print $2}'`
#echo engineVersionCode: ${engineVersionCode}

builder_jar_dir=`pwd`/container-builder-repkg/build/libs/

for file in `ls ${builder_jar_dir}`
do
    # echo "test file:"${file}
    if [[ ${file} =~ "container-builder-repkg" ]] && [[ ${file} =~ ".jar" ]] &&  [[ ${file} =~ ${engineVersionCode} ]];then
        builder_jar=${builder_jar_dir}${file}
        builder_jar_file_name=${file}
    fi
done

#builder_jar=`pwd`/container-builder-repkg/build/libs/container-builder-repkg-1.0.0.jar

if [ ! -f ${builder_jar} ] ;then
     echo "can not find container build jar in path:${builder_jar}"
     exit -1
fi

rm -rf ${script_dir}/dist

if [ ! -d ${script_dir}/dist ] ;then
    mkdir ${script_dir}/dist
fi

if [ ! -d ${script_dir}/dist/res ] ;then
    mkdir ${script_dir}/dist/res
fi
cp ${builder_jar} ${script_dir}/dist/res/

./gradlew container-builder-transformer:assemble
transformer_jar=`pwd`/container-builder-transformer/build/libs/EngineBinTransformer-1.0.jar
echo transform ratel builder format from jar to dex with transformer_jar ${transformer_jar}
echo java -jar ${transformer_jar} -s ${script_dir}/dist/res/${builder_jar_file_name}
java -jar ${transformer_jar} -s ${script_dir}/dist/res/${builder_jar_file_name}


cp ${script_dir}/hermes_key ${script_dir}/dist/res/
cp ${script_dir}/ratel.sh ${script_dir}/dist/
cp ${script_dir}/ratel.bat ${script_dir}/dist/
date > ${script_dir}/dist/res/build_timestamp.txt
echo ${builder_jar_file_name} > ${script_dir}/dist/res/ratel_version.txt


cd ${script_dir}/dist/
zip -r dist_custom.zip ./*
mv dist_custom.zip ../
