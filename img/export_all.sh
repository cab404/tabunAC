#!/bin/bash

# Exports svg parts as png using Inkscape CLI.
# @author cab404

FILE="symbols_flat.svg"
IDS="0 0-1 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37"

for ID in $IDS
do
	inkscape $FILE --without-gui --export-id=$ID --export-id-only --export-use-hints | grep "exported"
done
