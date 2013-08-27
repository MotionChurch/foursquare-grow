#!/bin/sh

INPUT=$1
NAME=$(basename $INPUT .jpg)
TMP=tmp/
OUT=out/

mkdir -p $TMP $OUT

convert -scale x134 -crop 134x155+0+0 -gravity center -extent 134x155 -background white $INPUT $TMP/$NAME.jpg
composite -gravity center frame.png $TMP/$NAME.jpg $OUT/$NAME.jpg
composite -gravity center frame-hover.png $TMP/$NAME.jpg $OUT/$NAME-hover.jpg


