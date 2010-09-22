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

if [ -z "$JAVAMEMOPTIONS" ]; then
    JAVAMEMOPTIONS="-Xmx256m"
fi

# ******************************************************************
# ** Set java runtime options                                     **
# ** Set JAVAMEMOPTIONS to a higher value e.g. -Xms512m -Xmx512m  **
# ** in case you run out of memory.                               **
# ******************************************************************

OPT="$JAVAMEMOPTIONS -cp $CLASSPATH -Dorg.mortbay.util.URI.charset=UTF-8 -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"

if [ -n "$PENTAHO_INSTALLED_LICENSE_PATH" ]; then
     export OPT="$OPT -Dpentaho.installed.licenses.file=$PENTAHO_INSTALLED_LICENSE_PATH"
fi
# ******************************************************************
# ** Set up the options for JAAS                                  **
# ******************************************************************

if [ ! "x$JAAS_LOGIN_MODULE_CONFIG" = "x" -a ! "x$JAAS_LOGIN_MODULE_NAME" = "x" ]; then
	OPT=$OPT" -Djava.security.auth.login.config=$JAAS_LOGIN_MODULE_CONFIG"
	OPT=$OPT" -Dloginmodulename=$JAAS_LOGIN_MODULE_NAME"
fi

# ***************
# ** Run...    **
# ***************

"$_PENTAHO_JAVA" $OPT org.pentaho.di.www.Carte "${1+$@}"
