#!/bin/sh

XCODEPATH=$(xcode-select -print-path)
PLATFORMBASE=${XCODEPATH}/Platforms
IOSSDKVERSION=8.0

set -e

pushd ../../../../third/x264_build

SCRIPT_DIR=$( (cd -P $(dirname $0) && pwd) )
DIST_DIR_BASE=${DIST_DIR_BASE:="$SCRIPT_DIR/output/ios/dist"}

ARCHS=${ARCHS:-"armv7 armv7s arm64"}

for ARCH in $ARCHS
do
    DIST_DIR=$DIST_DIR_BASE-$ARCH
    mkdir -p $DIST_DIR

    PLATFORM="${PLATFORMBASE}/iPhoneOS.platform"
    IOSSDK=iPhoneOS${IOSSDKVERSION}

    EXTRA_CFLAGS="-arch ${ARCH} -mfpu=neon"
    EXTRA_CXXFLAGS="$EXTRA_CFLAGS"
    EXTRA_LDFLAGS="$EXTRA_CFLAGS -L${PLATFORM}/Developer/SDKs/${IOSSDK}.sdk/usr/lib/system"

    case $ARCH in
    armv7)
        EXTRA_CONFIG="--enable-static --enable-pic --disable-cli --host=arm-apple-darwin"
        ;;
    armv7s)
        EXTRA_CONFIG="--enable-static --enable-pic --disable-cli --cpu=swift --host=arm-apple-darwin"
        ;;
    arm64)
        EXTRA_CONFIG="--enable-static --enable-pic --disable-cli --host=aarch64-apple-darwin"
        EXPORT="GASPP_FIX_XCODE5=1"
        ;;
    *)
        echo "Unsupported architecture ${ARCH}"
        exit 1
        ;;
    esac

    SYSROOT=${PLATFORM}/Developer/SDKs/${IOSSDK}.sdk
    export PATH=${SCRIPT_DIR}/tools:$PATH
    echo $PATH
    #export RANLIB=${PLATFORM}/Developer/usr/bin/ranlib
    export RANLIB=ranlib
    export CC="xcrun -sdk iphoneos clang -arch $ARCH"
    export AS="gas-preprocessor.pl xcrun -sdk iphoneos clang -arch $ARCH"

    echo "Configuring x264 for $ARCH..."
    ./configure \
    --prefix=$DIST_DIR \
    $EXTRA_CONFIG \
    --extra-cflags="$EXTRA_CFLAGS" \
    --extra-ldflags="$EXTRA_LDFLAGS"

    echo "Installing x264 for $ARCH..."
    make clean -i
    make -j8 V=1
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

LIB_DIR="$SCRIPT_DIR/../../lib/ios/uarch"

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

    cp $OUTPUT $LIB_DIR/libh264enc.a
done
popd





