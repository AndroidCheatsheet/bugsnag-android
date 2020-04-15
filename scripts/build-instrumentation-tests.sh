./scripts/install-ndk.sh

./gradlew assembleAndroidTest --stacktrace -PABI_FILTERS=arm64-v8a,armeabi,armeabi-v7a,x86,x86_64
