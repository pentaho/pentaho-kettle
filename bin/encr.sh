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

OPT="-cp $CLASSPATH"

if [ -n "$PENTAHO_INSTALLED_LICENSE_PATH" ]; then
     export OPT="$OPT -Dpentaho.installed.licenses.file=$PENTAHO_INSTALLED_LICENSE_PATH"
fi
# ***************
# ** Run...    **
# ***************

"$_PENTAHO_JAVA" $OPT org.pentaho.di.core.encryption.Encr "${1+$@}"




