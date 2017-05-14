@echo off
echo Start Build Apk

call gradlew clean
call gradlew assembleProductionRelease -Dchannel=yingyongbao
call gradlew assembleProductionRelease -Dchannel=360
call gradlew assembleProductionRelease -Dchannel=vivo,pc6
call gradlew assembleProductionRelease -Dchannel=flyme
call gradlew assembleProductionRelease -Dchannel=wandoujia
call gradlew assembleProductionRelease -Dchannel=baidu

echo Build Apk Complete