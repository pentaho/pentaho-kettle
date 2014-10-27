#!/bin/sh

BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null

if [ "$1" = "-x" ]; then
  set LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASEDIR/lib
  export LD_LIBRARY_PATH
  export OPT="-Xruntracer $OPT"
  shift
fi

export IS_KITCHEN="true"

"$DIR/spoon.sh" -main org.pentaho.di.kitchen.Kitchen "$@"
