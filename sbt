#!/bin/bash

SBT_BOOT_DIR=$HOME/.sbt/boot/

if [ ! -d "$SBT_BOOT_DIR" ]; then
  mkdir -p $SBT_BOOT_DIR
fi

java -Dfile.encoding=UTF8 -Xmx1024M -XX:+CMSClassUnloadingEnabled -XX:+UseCompressedOops -XX:MaxPermSize=768m \
	$SBT_EXTRA_PARAMS \
	-Dbuild.time="`date`" \
	-Dsbt.boot.directory=$SBT_BOOT_DIR \
	-jar `dirname $0`/sbt-launch.jar "$@"
