#!/usr/bin/env bash
echo "Start Build Apk"

./gradlew clean
./gradlew assembleProductionRelease -Dchannel=yingyongbao
./gradlew assembleProductionRelease -Dchannel=360
./gradlew assembleProductionRelease -Dchannel=vivo,pc6
./gradlew assembleProductionRelease -Dchannel=flyme
./gradlew assembleProductionRelease -Dchannel=wandoujia
./gradlew assembleProductionRelease -Dchannel=baidu
./gradlew assembleProductionRelease -Dchannel=kinglloy
./gradlew assembleProductionRelease -Dchannel=google

echo "Build Apk Complete"
