#!/usr/bin/env bash

cd `dirname $0`
cd ../../

version_text=`cat base-lib-ratel-api/ratel-api/build.gradle | grep version | grep -v options`
echo ${version_text}

cd base-lib-ratel-api/ratel-api/

if [[ ${version_text} =~ "SNAPSHOT" ]] ;then
    ./gradlew publishMavenJavaPublicationToSonatypeSnapshotRepository --no-daemon --no-parallel
else
    ./gradlew publishMavenJavaPublicationToSonatypeRepository --no-daemon --no-parallel
fi
