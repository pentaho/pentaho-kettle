#!/bin/sh

INITIALDIR="`pwd`"
BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null

"$DIR/spoon.sh" -main org.pentaho.pdi.spark.driver.app.builder.SparkDriverAppBuilder "$@"
