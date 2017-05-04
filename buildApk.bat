@echo off
echo Start Build Apk

call gradlew clean
call gradlew assembleProduction -Dchannel=yingyongbao
call gradlew assembleProduction -Dchannel=360
call gradlew assembleProduction -Dchannel=vivo,pc6

echo Build Apk Complete