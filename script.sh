#!bash

gradle clean
rm -rvf ./release/libs/*
rm -rvf ./release/package/source/rongCloud.jar

gradle build

cp -v ./lib/build/intermediates/bundles/release/classes.jar ./release/libs/lib.jar
cp -v ./msg/build/intermediates/bundles/release/classes.jar ./release/libs/msg.jar
cp -v ./ipc/build/intermediates/bundles/release/classes.jar ./release/libs/ipc.jar
cp -v ./apicloud/build/intermediates/bundles/release/classes.jar ./release/libs/apicloud.jar

cd release
gradle build

cd ..
cp -v ./release/build/libs/release.jar ./release/package/source/rongCloud2.jar
