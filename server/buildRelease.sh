#!/bin/bash

set -e

#
# Makes a fat jar with the mod + outside libs
#

MVERSE_VERSION=0.2

rm -rf ./tmp
mkdir -p ./tmp
mkdir -p ./build/mverse-release

(cd tmp; unzip -uo "../lib/*.jar")
(cd tmp; unzip -uo ../build/libs/mverse-server-$MVERSE_VERSION.jar)

jar -cvf mverse-server-$MVERSE_VERSION.jar -C tmp .

mv mverse-server-$MVERSE_VERSION.jar ./build/mverse-release

rm -rf ./tmp
