#!/usr/bin/env bash

adb root
cd `dirname $0`

adb pull /data/data/com.virjar.ratel.demoapp/app_ratel/ratel_resource/sandHookCache/hookers/
cd hookers/
zip -r dex_maker_opt.zip ./*
mv dex_maker_opt.zip ../../
cd ../
rm -rf hookers
cd ../