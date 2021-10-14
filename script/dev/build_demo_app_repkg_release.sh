#!/usr/bin/env bash

# 使用dev环境的container环境构建，这样主要是方便测试container的逻辑
now_dir=`pwd`
cd `dirname $0`
script_dir=`pwd`
cd ../../
echo "build demoapp"
./gradlew demoapp:assembleRelease
echo "build crack module for demoApp"
./gradlew crack-demoapp:assembleDebug
./script/build_ratel_repkg.sh demoapp/build/outputs/apk/release/demoapp-release.apk \
crack-demoapp/build/outputs/apk/debug/crack-demoapp-debug.apk