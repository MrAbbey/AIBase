#!/usr/bin/env bash


#删除上一次打包的apk
basedir=`cd $(dirname $0); pwd -P`
rm -rf ${basedir}/apk

# 调用gradle打包脚本
./gradlew clean
./gradlew assembleRelease
# 拷贝apk到当前目录的apk文件夹中
./gradlew makeCopy
