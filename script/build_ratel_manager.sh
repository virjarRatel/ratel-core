#!/usr/bin/env bash

# 使用dev环境的container环境构建，这样主要是方便测试container的逻辑
now_dir=`pwd`
cd `dirname $0`
script_dir=`pwd`

./create-dist.sh

cd ..
rm -rf ratelmanager/build/outputs/apk/release
./gradlew ratelmanager:assembleRelease

cd ratelmanager/build/outputs/apk/release
rm_release_dir=`pwd`
for file in `ls ${rm_release_dir}`
do
    if [[ ${file} =~ "RatelManager" ]];then
        out_apk=${rm_release_dir}/${file}
    fi
done
if [ -f ${out_apk} ] ;then
    echo "RatelManager apk file:"${out_apk}
else
    echo "can not find  RatelManager apk file in path:${rm_release_dir}"
    echo -1
fi

cd ${now_dir}