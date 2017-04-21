#!/bin/sh

INITIALDIR="`pwd`"
BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null

if [ -z "$PENTAHO_DI_JAVA_OPTIONS" ]; then
	export PENTAHO_DI_JAVA_OPTIONS="-Xms1024m -Xmx3072m -XX:MaxPermSize=256m"
fi

"$DIR/spoon.sh" -main org.pentaho.pdi.spark.driver.app.builder.SparkDriverAppBuilder "$@"
