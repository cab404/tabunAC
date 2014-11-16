#!/bin/sh
bash export_all.sh;
bash android_icons.sh;
rm *.png;
cp -rfv drawable*/ ../res;
rm -rf drawable-*

