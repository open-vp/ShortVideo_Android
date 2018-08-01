#!/bin/sh

set -e

SCRIPT_DIR=$( (cd -P $(dirname $0) && pwd) )
DIST_DIR_BASE=${DIST_DIR_BASE:="$SCRIPT_DIR/../../lib/ios"}

echo $DIST_DIR_BASE

ARCHS=${ARCHS:-"armv7 armv7s arm64"}

for ARCH in $ARCHS
do

    echo "Make arch $ARCH"
    make -f makefile_ios clean
    make -f makefile_ios ARCH=$ARCH
    make -f makefile_ios clean
    cd $SCRIPT_DIR

done

for ARCH in $ARCHS
do
    if [ -d $DIST_DIR_BASE/$ARCH ]
    then
        MAIN_ARCH=$ARCH
    fi
done

if [ -z "$MAIN_ARCH" ]
then
    echo "Please compile an architecture"
    exit 1
fi


OUTPUT_DIR="$DIST_DIR_BASE/uarch"

#rm -rf $OUTPUT_DIR
#mkdir -p $OUTPUT_DIR

for LIB in $DIST_DIR_BASE/$MAIN_ARCH/*.a
do
    LIB=`basename $LIB`
    LIPO_CREATE=""
    for ARCH in $ARCHS
    do
        if [ -d $DIST_DIR_BASE/$ARCH ]
        then
            LIPO_CREATE="$LIPO_CREATE -arch $ARCH $DIST_DIR_BASE/$ARCH/$LIB"
            echo $LIPO_CREATE
        fi
    done
    OUTPUT="$OUTPUT_DIR/$LIB"
    echo "Creating: $OUTPUT"
    xcrun -sdk iphoneos lipo -create $LIPO_CREATE -output $OUTPUT
    xcrun -sdk iphoneos lipo -info $OUTPUT
done




