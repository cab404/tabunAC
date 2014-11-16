#/bin/bash
#@author cab404
# Simple tool for converting multiple icons fo Android app

mkdir -p drawable-mdpi
mkdir -p drawable-hdpi
mkdir -p drawable-xhdpi
mkdir -p drawable-xxhdpi
# mkdir -p drawable-xxxhdpi

for F in $(ls | grep .png)
do
	convert $F -resize 48x48 drawable-mdpi/$F
	convert $F -resize 72x72 drawable-hdpi/$F
	convert $F -resize 96x96 drawable-xhdpi/$F
	convert $F -resize 144x144 drawable-xxhdpi/$F
#	convert $F -resize 192x192 drawable-xxxhdpi/$F
done
