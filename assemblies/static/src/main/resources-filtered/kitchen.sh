#!/bin/sh
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2030-06-15
# ******************************************************************************



INITIALDIR="`pwd`"
BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null

if [ "$1" = "-x" ]; then
  set LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$BASEDIR/lib"
  export LD_LIBRARY_PATH
  export OPT="-Xruntracer $OPT"
  shift
fi

export IS_KITCHEN="true"

"$DIR/spoon.sh" -main org.pentaho.di.kitchen.Kitchen -initialDir "$INITIALDIR/" "$@"
