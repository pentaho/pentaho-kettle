#!/bin/sh

BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null
"$DIR/spoon.sh" -main org.pentaho.di.core.encryption.Encr "$@"
