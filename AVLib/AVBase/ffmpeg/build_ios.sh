#!/bin/sh

XCODEPATH=$(xcode-select -print-path)
PLATFORMBASE=${XCODEPATH}/Platforms
IOSSDKVERSION=8.0

set -e

SCRIPT_DIR=$( (cd -P $(dirname $0) && pwd) )
DIST_DIR_BASE=${DIST_DIR_BASE:="$SCRIPT_DIR/output/ios/dist"}


ARCHS=${ARCHS:-"armv7 armv7s arm64"}

for ARCH in $ARCHS
do
  

    DIST_DIR=$DIST_DIR_BASE-$ARCH
    mkdir -p $DIST_DIR

    PLATFORM="${PLATFORMBASE}/iPhoneOS.platform"
    IOSSDK=iPhoneOS${IOSSDKVERSION}

    EXTRA_CFLAGS="-arch ${ARCH} -mfpu=neon -miphoneos-version-min=${IOSSDKVERSION}"
    EXTRA_CXXFLAGS="$EXTRA_CFLAGS"
    EXTRA_LDFLAGS="$EXTRA_CFLAGS -L${PLATFORM}/Developer/SDKs/${IOSSDK}.sdk/usr/lib/system"

    case $ARCH in
        armv7)
            EXTRA_CONFIG="--arch=arm --target-os=darwin --enable-cross-compile --cpu=cortex-a8 --disable-armv5te --disable-armv6 --disable-armv6t2 --enable-pic"
            ;;
        armv7s)
            EXTRA_CONFIG="--arch=arm --target-os=darwin --enable-cross-compile --cpu=cortex-a9 --disable-armv5te --disable-armv6 --disable-armv6t2 --enable-pic"
            ;;
        arm64)
            EXTRA_CONFIG="--arch=arm64 --target-os=darwin --enable-cross-compile --disable-armv5te --disable-armv6 --disable-armv6t2 --enable-pic"
            EXPORT="GASPP_FIX_XCODE5=1"
            ;;
        *)
            echo "Unsupported architecture ${ARCH}"
            exit 1
            ;;
    esac
    ARMCC="xcrun -sdk iphoneos clang"
    export RANLIB=${PLATFORM}/Developer/usr/bin/ranlib

    echo "Configuring ffmpeg for $ARCH..."
    ./configure \
    --prefix=$DIST_DIR \
    --cc="${ARMCC}" \
    --extra-ldflags="${EXTRA_LDFLAGS}" \
    --extra-cflags="${EXTRA_CFLAGS}" \
    --extra-cxxflags="${EXTRA_CXXFLAGS}" \
    ${EXTRA_CONFIG} \
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
    --disable-demuxers \
    --disable-programs \
    --enable-decoder=h264 \
    --enable-decoder=vp8 \
    --enable-decoder=wmv3

    echo "Installing ffmpeg for $ARCH..."
    make clean -i
    make -j8 V=1 $EXPORT
    make install
	make clean -i
    cd $SCRIPT_DIR

    if [ -d $DIST_DIR/bin ]
    then
      rm -rf $DIST_DIR/bin
    fi
    if [ -d $DIST_DIR/share ]
    then
      rm -rf $DIST_DIR/share
    fi
done


for ARCH in $ARCHS
do
  if [ -d $DIST_DIR_BASE-$ARCH ]
  then
    MAIN_ARCH=$ARCH
  fi
done

if [ -z "$MAIN_ARCH" ]
then
  echo "Please compile an architecture"
  exit 1
fi


OUTPUT_DIR="$DIST_DIR_BASE-uarch"
rm -rf $OUTPUT_DIR

mkdir -p $OUTPUT_DIR/lib $OUTPUT_DIR/include

for LIB in $DIST_DIR_BASE-$MAIN_ARCH/lib/*.a
do
  LIB=`basename $LIB`
  LIPO_CREATE=""
  for ARCH in $ARCHS
  do
    if [ -d $DIST_DIR_BASE-$ARCH ]
    then
      LIPO_CREATE="$LIPO_CREATE-arch $ARCH $DIST_DIR_BASE-$ARCH/lib/$LIB "
    fi
  done
  OUTPUT="$OUTPUT_DIR/lib/$LIB"
  echo "Creating: $OUTPUT"
  xcrun -sdk iphoneos lipo -create $LIPO_CREATE -output $OUTPUT
  xcrun -sdk iphoneos lipo -info $OUTPUT
done

echo "Copying headers from dist-$MAIN_ARCH..."
cp -R $DIST_DIR_BASE-$MAIN_ARCH/include/* $OUTPUT_DIR/include
