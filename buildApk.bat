@echo off
echo Start Build Apk

call gradlew clean
call gradlew assembleProductionRelease -Dchannel=kinglloy
call gradlew assembleProductionRelease -Dchannel=yingyongbao
call gradlew assembleProductionRelease -Dchannel=360 -Dapp_name=Style艺术壁纸
call gradlew assembleProductionRelease -Dchannel=vivo
call gradlew assembleProductionRelease -Dchannel=flyme
call gradlew assembleProductionRelease -Dchannel=wandoujia
call gradlew assembleProductionRelease -Dchannel=baidu -Dapp_name=Style艺术壁纸
call gradlew assembleGoogleprodRelease -Dchannel=google
call gradlew assembleProductionRelease -Dchannel=huawei -Dapp_name=Style艺术壁纸

echo Build Apk Complete