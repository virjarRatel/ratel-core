#!/usr/bin/env bash

# 使用dev环境的container环境构建，这样主要是方便测试container的逻辑
now_dir=`pwd`
cd `dirname $0`
script_dir=`pwd`
cd ..

if [[ "$*" =~ '-PforceUserDexMakder=true' ]] ;then
    ./gradlew container-builder-repkg:assemble -PforceUserDexMakder=true
else
    ./gradlew container-builder-repkg:assemble
fi
if [ $? != 0 ] ;then
    echo "builder jar assemble failed"
    exit $?
fi

engineVersionCode=`cat build.gradle | grep ratelEngineVersion | grep -v ratelEngineVersionCode | awk '{print $3}' | awk -F "\"" '{print $2}'`
echo engineVersionCode: ${engineVersionCode}

builder_jar_dir=`pwd`/container-builder-repkg/build/libs/

for file in `ls ${builder_jar_dir}`
do
    echo "test file:"${file}
    if [[ ${file} =~ "container-builder-repkg" ]] && [[ ${file} =~ ".jar" ]] &&  [[ ${file} =~ ${engineVersionCode} ]];then
        builder_jar=${builder_jar_dir}${file}
    fi
done

#builder_jar=`pwd`/container-builder-repkg/build/libs/container-builder-repkg-1.0.0.jar

if [ -f ${builder_jar} ] ;then
    echo "use ${builder_jar}"
else
    echo "can not find container build jar in path:${builder_jar}"
    echo -1
fi

key_file=${script_dir}/monthly_temp.txt

has_certificate='false'
is_extract_apk_task='false'

for ratel_param in $@
do
    # echo ${ratel_param}
    if [ ${ratel_param} = '-c' ] ;then
        has_certificate='true'
    elif [ ${ratel_param} = '--certificate' ] ;then
         has_certificate='true'
    elif [ ${ratel_param} = '-x' ] ;then
         is_extract_apk_task='true'
    elif [ ${ratel_param} = '--extract' ] ;then
         is_extract_apk_task='true'
    fi


done

cd ${now_dir}

out_apk_path=`java -jar ${builder_jar} -t -s -w ~/.ratel-working-repkg $*`
if [ $? != 0 ] ;then
    echo "call builder jar failed"
    exit $?
fi
echo "assemble new apk for $*"

if [ ${has_certificate} = 'true' ] ; then
    java -jar ${builder_jar}  -s -w ~/.ratel-working-repkg $*
else
    echo "build with default certificate"
    java -jar ${builder_jar}  -s -w ~/.ratel-working-repkg -c ${key_file}  $*
fi



if [ $? != 0 ] ;then
    echo "assemble ratel apk failed"
    exit $?
fi


echo "the final output apk file is :${out_apk_path}"

