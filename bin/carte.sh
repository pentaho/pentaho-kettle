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

# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 512m to higher values in case you run out of memory   **
# ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
# ** (JAVAMEMOPTIONS is there for compatibility reasons)          **
# ******************************************************************

if [ -z "$JAVAMEMOPTIONS" ]; then
    JAVAMEMOPTIONS="-Xmx512m"
fi

if [ -z "$PENTAHO_DI_JAVA_OPTIONS" ]; then
    PENTAHO_DI_JAVA_OPTIONS=JAVAMEMOPTIONS
fi

OPT="$PENTAHO_DI_JAVA_OPTIONS -cp $CLASSPATH -Dorg.mortbay.util.URI.charset=UTF-8 -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"

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
