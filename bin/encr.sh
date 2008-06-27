#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR

CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-engine-3.0.jar

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

OPT="-cp $CLASSPATH"

# ***************
# ** Run...    **
# ***************

$JAVA_BIN $OPT org.pentaho.di.core.encryption.Encr "${1+$@}"




