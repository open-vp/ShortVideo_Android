#!/bin/sh
 
./config_android.sh

make clean
make -j8 V=1
make install
make clean 