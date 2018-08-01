export NDK=/opt/android-ndk-r10d
PLATFORM=$NDK/platforms/android-15/arch-arm
PREBUILT=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt
PREFIX=/home/faac

    CFLAGS="-fpic -DANDROID -fpic -mthumb-interwork -ffunction-sections -funwind-tables -fstack-protector -fno-short-enums -D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__ -Wno-psabi -march=armv5te -mtune=xscale -msoft-float -mthumb -Os -fomit-frame-pointer -fno-strict-aliasing -finline-limit=64 -DANDROID -Wa,--noexecstack -MMD -MP "
#FLAGS="--host=arm-androideabi-linux --enable-static --disable-shared --prefix=$HOME --enable-armv5e "
    CROSS_COMPILE=$PREBUILT/linux-x86_64/bin/arm-linux-androideabi-
    export CPPFLAGS="$CFLAGS"
    export CFLAGS="$CFLAGS"
    export CXXFLAGS="$CFLAGS"
    export CXX="${CROSS_COMPILE}g++ --sysroot=${PLATFORM}"
    export LDFLAGS="$LDFLAGS"
    export CC="${CROSS_COMPILE}gcc --sysroot=${PLATFORM}"
    export NM="${CROSS_COMPILE}nm"
    export STRIP="${CROSS_COMPILE}strip"
    export RANLIB="${CROSS_COMPILE}ranlib"
    export AR="${CROSS_COMPILE}ar"

./configure --program-prefix=$PREFIX \
--without-mp4v2 \
--host=arm-linux

make
cp ./libfaac/.libs/*.a $PREFIX/lib
cp ./libfaac/.libs/*.so $PREFIX/lib
cp ./include/*.h $PREFIX/include