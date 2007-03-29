#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=$(dirname $0)
CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle.jar

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

JAVA_BIN=java

# circumvention for the IBM JVM behavior (seems to be a problem with the IBM JVM native compiler)
if [ `uname -s` = "OS400" ]
then
  CLASSPATH=${CLASSPATH}:libswt/aix/swt.jar
fi


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 128m to higher values in case you run out of memory.  **
# ******************************************************************

if [ -z "$JAVAMAXMEM" ]; then
  JAVAMAXMEM="256"
fi

OPT="-Xmx${JAVAMAXMEM}m -cp $CLASSPATH -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD"

if [ "$1" = "-x" ]; then
  export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASEDIR/libext
  OPT="-Xruntracer $OPT"
  shift
fi

# ***************
# ** Run...    **
# ***************

$JAVA_BIN $OPT be.ibridge.kettle.pan.Pan "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"

