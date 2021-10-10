#!/usr/bin/env bash

cd `dirname $0`
cd ../..

version_text=`cat base-lib-ratel-extension/ratelextension/build.gradle | grep version | grep -v options`
cd base-lib-ratel-extension/ratelextension
echo ${version_text}
if [[ ${version_text} =~ "SNAPSHOT" ]] ;then
    ./gradlew publishMavenJavaPublicationToSonatypeSnapshotRepository --no-daemon --no-parallel
else
    ./gradlew publishMavenJavaPublicationToSonatypeRepository --no-daemon --no-parallel
fi
