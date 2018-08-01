SRCPATH		=.
ndk_root	=/opt/android-ndk-r11c
sys_root	=${ndk_root}/platforms/android-15/arch-arm
ndk_lib		=${sys_root}/usr/lib
ndk_inc		=${sys_root}/usr/include
ndk_tool	=${ndk_root}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
ndk_bin		=${ndk_tool}/bin
ndk_c++inc  =${ndk_root}/sources/cxx-stl/system/include
stl_lib		=${ndk_root}/sources/cxx-stl/stlport/libs/armeabi

ARCH		=ARM
SYS			=LINUX

CC			=${ndk_bin}/arm-linux-androideabi-gcc
CXX			=${ndk_bin}/arm-linux-androideabi-gcc
LD			=${ndk_bin}/arm-linux-androideabi-gcc
AR			=${ndk_bin}/arm-linux-androideabi-ar rc 
RANLIB		=${ndk_bin}/arm-linux-androideabi-ranlib
STRIP		=${ndk_bin}/arm-linux-androideabi-strip
AS			=${ndk_bin}/arm-linux-androideabi-gcc
RC			=

RCFLAGS		:=

COMMONFLAGS := -MMD -MP -fPIC  -ffunction-sections  -funwind-tables  -fstack-protector 
COMMONFLAGS	+= -D__ARM_ARCH_5__ -D__ARM_ARCH_5T__  -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__ -D_FS_OS_ANDROID
COMMONFLAGS += -march=armv7-a  -mfloat-abi=softfp  -mfpu=vfp 
COMMONFLAGS += -mthumb  -Os  -fomit-frame-pointer  -fno-strict-aliasing  -finline-limit=64
COMMONFLAGS += -DANDROID -Wa,--noexecstack
COMMONFLAGS += -I. -I${ndk_inc} -I../../../../basecore/framecore/include

CFLAGS		:= $(COMMONFLAGS)
CXXFLAGS 	:= $(COMMONFLAGS) -fno-exceptions -fno-rtti -I${ndk_c++inc} -I${ndk_root}/sources/cxx-stl/stlport/stlport
ASFLAGS		:= $(COMMONFLAGS) -DHIGH_BIT_DEPTH=0 -DBIT_DEPTH=8
LDFLAGS		:=-L${ndk_lib} -L${stl_lib}  -shared --sysroot=${sys_root} -Wl,--no-undefined -Wl,-z,noexecstack -Wl,-z,relro -Wl,-z,now -lc -lm

LIBS		:= -lstlport_static -lstdc++

DEBUG_CXXFLAGS	:= ${CXXFLAGS} -g -DDEBUG -D_DEBUG
RELEASE_CXXFLAGS:= ${CXXFLAGS} -DNDEBUG

DEBUG_CFLAGS  	:= ${CFLAGS} -g -DDEBUG -D_DEBUG
RELEASE_CFLAGS	:= ${CFLAGS} -DNDEBUG

DEBUG_LDFLAGS   := ${LDFLAGS}
RELEASE_LDFLAGS := ${LDFLAGS}

OUTPUT_OBJDIR	:= ../../../../obj/android/
OUTPUT_LIBDIR	:= ../../../../lib/android/
OUTPUT_BINDIR	:= ../../../../bin/android/
OUTPUT		 	:=