#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv

CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-core.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-db.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-engine.jar

# **************************************************
# ** JDBC & other libraries used by Kettle:       **
# **************************************************

for f in `find $BASEDIR/libext -type f -name "*.jar"` `find $BASEDIR/libext -type f -name "*.zip"`
do
  CLASSPATH=$CLASSPATH:$f
done


# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

# circumvention for the IBM JVM behavior (seems to be a problem with the IBM JVM native compiler)
if [ `uname -s` = "OS400" ]
then
  CLASSPATH=${CLASSPATH}:$BASEDIR/libswt/aix/swt.jar
fi


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 128m to higher values in case you run out of memory.  **
# ******************************************************************

if [ -z "$JAVAMAXMEM" ]; then
  JAVAMAXMEM="256"
fi

OPT="-Xmx${JAVAMAXMEM}m -cp $CLASSPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"
if [ -n "$PENTAHO_INSTALLED_LICENSE_PATH" ]; then
     export OPT="$OPT -Dpentaho.installed.licenses.file=$PENTAHO_INSTALLED_LICENSE_PATH"
fi

if [ "$1" = "-x" ]; then
  set LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASEDIR/libext
  export LD_LIBRARY_PATH
  OPT="-Xruntracer $OPT"
  shift
fi

# ***************
# ** Run...    **
# ***************

"$_PENTAHO_JAVA" $OPT org.pentaho.di.pan.Pan "${1+$@}"

