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
        builder_dex_file_name=$(echo ${builder_jar_file_name} | sed 's/\.[^.]*$//')"-dex.jar"
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
cp ${script_dir}/hermes_bksv1_key ${script_dir}/dist/res/
cp ${script_dir}/ratel.sh ${script_dir}/dist/
cp ${script_dir}/ratel.bat ${script_dir}/dist/
date > ${script_dir}/dist/res/build_timestamp.txt
echo ${builder_jar_file_name} > ${script_dir}/dist/res/ratel_version.txt

# create obt realase files
cd ${script_dir}/dist/


# release for super admin
cp ${script_dir}/monthly_temp.txt ${script_dir}/dist/res/monthly_temp.txt

cd ${root_dir}
builder_helper_jar=${root_dir}/container-builder-helper/build/libs/BuilderHelper-1.0.jar

# jar包内置资源的优化
java -jar ${builder_helper_jar} OPTIMIZE_BUILDER_RESOURCE --rdp -i ${script_dir}/dist/res/${builder_jar_file_name}  -o ${script_dir}/dist/res/${builder_jar_file_name}

# jar包工具链转化为dex
java -jar ${builder_helper_jar} TRANSFORM_BUILDER_JAR -s ${script_dir}/dist/res/${builder_jar_file_name} -d ${script_dir}/dist/res/${builder_dex_file_name}

# 需要注意，需先行转化为dex，后面再进行class优化，因为class优化source和destination是相同的，这会导致jar文件被修改
# jar包代码本身代码优化
java -jar ${builder_helper_jar} OPTIMIZE_BUILDER_CLASS -i ${script_dir}/dist/res/${builder_jar_file_name}


# 拷贝到 rm assets 目录下
if [ ! -d ${root_dir}/ratelmanager/src/main/assets ] ;then
  mkdir ${root_dir}/ratelmanager/src/main/assets
fi
if [ -f ${root_dir}/ratelmanager/src/main/assets/container-builder-repkg-dex.jar ] ;then
  rm ${root_dir}/ratelmanager/src/main/assets/container-builder-repkg-dex.jar
fi
cp ${script_dir}/dist/res/${builder_dex_file_name} ${root_dir}/ratelmanager/src/main/assets/container-builder-repkg-dex.jar


cd ${root_dir}




if [ -d /opt/ratel/ ] ;then
  # virjar 电脑的特殊逻辑，我在电脑上配置了环境变量，并将ratel的脚本文件夹配置到 /opt/ratel下
  # 所以可以在任何地方直接调用  ratel.sh
  cp -r ${script_dir}/dist/* /opt/ratel/
fi

cd ${script_dir}/dist/
zip -r dist.zip ./*
mv dist.zip ../
