#!/usr/bin/env bash
echo "Start Build Apk"

./gradlew clean
./gradlew assembleProductionRelease -Dchannel=yingyongbao
./gradlew assembleProductionRelease -Dchannel=360 -Dapp_name=Style艺术壁纸
./gradlew assembleProductionRelease -Dchannel=vivo,pc6
./gradlew assembleProductionRelease -Dchannel=flyme
./gradlew assembleProductionRelease -Dchannel=wandoujia
./gradlew assembleProductionRelease -Dchannel=baidu -Dapp_name=Style艺术壁纸
./gradlew assembleProductionRelease -Dchannel=kinglloy
./gradlew assembleProductionRelease -Dchannel=google
./gradlew assembleProductionRelease -Dchannel=huawei

echo "Build Apk Complete"
