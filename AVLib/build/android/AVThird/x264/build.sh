#!/bin/bash
SRCPATH=.
ndk_root=/usr/local/android-ndk-r11c
sys_root=${ndk_root}/platforms/android-15/arch-arm
ndk_lib=${sys_root}/usr/lib
ndk_inc=${sys_root}/usr/include
ndk_tool=${ndk_root}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
ndk_bin=${ndk_tool}/bin

CC=${ndk_bin}/arm-linux-androideabi-gcc
CXX=${ndk_bin}/arm-linux-androideabi-gcc
LD=${ndk_bin}/arm-linux-androideabi-gcc
AS=${ndk_bin}/arm-linux-androideabi-gcc

pushd ../../../../third/x264_build_142
./configure --enable-static \
--enable-strip \
--enable-pic \
--chroma-format=420 \
--disable-avs \
--disable-cli \
--disable-opencl \
--host=arm-linux \
--cross-prefix=$ndk_bin/arm-linux-androideabi- \
--sysroot=$sys_root

make clean
make
cp libx264.a ../../lib/android/avcore/libh264enc.a
make clean
popd

