#! /bin/sh
clear
echo "BEGIN GEN RELEASE."

jar -cvf libs/sdk.jar -C ../sdk/build/intermediates/classes/debug/ .
gradle :release:assemble

unzip ../sdk/libs/lib-2.2.0.aar -d ./tmp
cp tmp/classes.jar libs/
cp tmp/jni/armeabi/libRongIMLib.so output/target/
cp tmp/jni/armeabi-v7a/libRongIMLib.so output/target/armeabi-v7a/
cp tmp/jni/x86/libRongIMLib.so output/target/x86/
cp -r tmp/res/ output/res_rongCloud/
cp tmp/AndroidManifest.xml output/res_rongCloud/
cp build/libs/release.jar output/source/

