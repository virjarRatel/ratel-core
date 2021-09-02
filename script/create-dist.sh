#!/usr/bin/env bash

# 使用dev环境的container环境构建，这样主要是方便测试container的逻辑
now_dir=`pwd`
cd `dirname $0`
script_dir=`pwd`
cd ..
root_dir=`pwd`
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


cp ${script_dir}/hermes_key ${script_dir}/dist/res/
cp ${script_dir}/ratel.sh ${script_dir}/dist/
cp ${script_dir}/ratel.bat ${script_dir}/dist/
date > ${script_dir}/dist/res/build_timestamp.txt
echo ${builder_jar_file_name} > ${script_dir}/dist/res/ratel_version.txt
cp ${script_dir}/monthly_temp_obt.txt ${script_dir}/dist/res/monthly_temp.txt

# create obt realase files
cd ${script_dir}/dist/
zip -r ratel-engine-obt-${engineVersionCode}.zip ./*
mv ratel-engine-obt-${engineVersionCode}.zip ../

# release for super admin
cp ${script_dir}/monthly_temp.txt ${script_dir}/dist/res/monthly_temp.txt

cd ${root_dir}
transformer_jar=${root_dir}/container-builder-helper/build/libs/BuilderHelper-1.0.jar
echo transform ratel builder format from jar to dex with transformer_jar ${transformer_jar}
echo java -jar ${transformer_jar} -s ${script_dir}/dist/res/${builder_jar_file_name}
java -jar ${transformer_jar} TRANSFORM_BUILDER_JAR -s ${script_dir}/dist/res/${builder_jar_file_name}

cd ${root_dir}




#if [ ! -d /opt/ratel/ ] ;then
#    mkdir /opt/ratel/
#fi
## 这里，放到系统指定目录，然后配置好环境变量，就可以直接命令行调用ratel了
#cp -r ${script_dir}/dist/* /opt/ratel/

cd ${script_dir}/dist/
zip -r dist.zip ./*
mv dist.zip ../
