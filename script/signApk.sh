#!/usr/bin/env bash

now_dir=`pwd`
cd `dirname $0`
script_dir=`pwd`
cd ..


jarsigner -verbose -storepass hermes -keystore ${script_dir}/hermes_key ${now_dir}/$1 hermes