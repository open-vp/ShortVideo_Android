#!/bin/sh

ndk_root=/usr/local/android-ndk-r11c
sys_root=${ndk_root}/platforms/android-15/arch-arm
ndk_lib=${sys_root}/usr/lib
ndk_inc=${sys_root}/usr/include
ndk_tool=${ndk_root}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
ndk_bin=${ndk_tool}/bin
ndk_cxxinc=${ndk_root}/sources/cxx-stl/system/include
stl_lib=${ndk_root}/sources/cxx-stl/stlport/libs/armeabi
ARCH=arm
SYS=linux

CC=${ndk_bin}/arm-linux-androideabi-gcc
CXX=${ndk_bin}/arm-linux-androideabi-gcc
LD=${ndk_bin}/arm-linux-androideabi-gcc
AR=${ndk_bin}/arm-linux-androideabi-ar
RANLIB=${ndk_bin}/arm-linux-androideabi-ranlib
STRIP=${ndk_bin}/arm-linux-androideabi-strip
AS=${ndk_bin}/arm-linux-androideabi-gcc
NM=${ndk_bin}/arm-linux-androideabi-nm


ASFLAGS="-DHIGH_BIT_DEPTH=0 -DBIT_DEPTH=8"

COMMONFLAGS="-MMD -MP -fPIC -ffunction-sections  -funwind-tables  -fstack-protector "
COMMONFLAGS+=" -D__ARM_ARCH_5__ -D__ARM_ARCH_5T__  -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__ "
COMMONFLAGS+=" -march=armv7-a  -mfloat-abi=softfp  -mfpu=vfp "
COMMONFLAGS+=" -mthumb  -Os  -fomit-frame-pointer  -fno-strict-aliasing  -finline-limit=64 "
COMMONFLAGS+=" -DANDROID -Wa,--noexecstack "
COMMONFLAGS+=" -I. -I${ndk_inc} "

CFLAGS="${COMMONFLAGS}"
CXXFLAGS="${COMMONFLAGS} -fno-exceptions -fno-rtti -I${ndk_cxxinc} -I${ndk_root}/sources/cxx-stl/stlport/stlport"
LDFLAGS="-L${ndk_lib} -L${stl_lib} -shared --sysroot=${sys_root} -Wl,--no-undefined -Wl,-z,noexecstack -Wl,-z,relro -Wl,-z,now -lc -lm"

LIBS="-llog -lstlport_static -lstdc++"

echo CFFLAGS=$CFLAGS
echo CXXFLAGS=$CXXFLAGS
echo LDFLAGS=$LDFLAGS
echo CC=$CC

pushd ../../../../third/ffmpeg-2.4.2
DIST_DIR=./output/android
./configure --prefix=$DIST_DIR \
		--target-os=$SYS \
		--arch=$ARCH \
		--disable-iconv \
		--disable-doc \
		--disable-indevs \
		--disable-ffmpeg \
		--disable-filters \
		--disable-network \
		--disable-debug \
		--disable-ffplay \
		--disable-ffserver \
		--disable-bsfs \
		--disable-encoders  \
		--disable-protocols  \
		--disable-parsers  \
		--disable-muxers  \
		--disable-decoders  \
		--enable-decoder=h264 \
		--enable-decoder=vp8 \
		--enable-decoder=wmv3 \
		--disable-demuxers \
		--disable-programs \
		--enable-cross-compile \
		--cross-prefix="$ndk_bin/arm-linux-androideabi-" \
		--cc="$CC" \
		--nm="$NM" \
		--ar="$AR" \
		--as="$AS" \
		--cxx="$CXX" \
		--ld="$LD" \
		--extra-cflags="$CFLAGS" \
		--extra-cxxflags="$CXXFLAGS" \
		--extra-ldflags="$LDFLAGS" \
		--extra-libs="$LIBS" \
		--enable-pic \
		--enable-neon \
		--enable-armv5te \
		--enable-armv6 \
		--enable-armv6t2 \
		--disable-stripping

make clean
make V=1
make install
make clean
popd
