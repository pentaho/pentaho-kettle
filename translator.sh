#!/bin/sh
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2029-07-20
# ******************************************************************************


# **************************************************
# ** Set these to the location of your mozilla
# ** installation directory.  Use a Mozilla with
# ** Gtk2 and Fte enabled.
# **************************************************

# export MOZILLA_FIVE_HOME=/usr/local/mozilla
# export LD_LIBRARY_PATH=/usr/local/mozilla

export MOZILLA_FIVE_HOME=/usr/lib/xulrunner-1.8.1/
export LD_LIBRARY_PATH=${MOZILLA_FIVE_HOME}:${LD_LIBRARY_PATH}

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=${KETTLE_HOME}
if [ -z "$KETTLE_HOME" ]; then
  BASEDIR=$(dirname $0)/dist/
fi
CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-core-TRUNK-SNAPSHOT.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-engine-TRUNK-SNAPSHOT.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-ui-swt-TRUNK-SNAPSHOT.jar
CLASSPATH=$CLASSPATH:lib/saxon-dom-9.1.0.8.jar

# echo $CLASSPATH

CLASSPATH=$CLASSPATH:$BASEDIR/libswt/jface.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/runtime.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/common.jar
CLASSPATH=$CLASSPATH:$BASEDIR/libswt/commands.jar

# **************************************************
# ** JDBC & other libraries used by Kettle:       **
# **************************************************

for f in `find $BASEDIR/lib -type f -name "*.jar"` `find $BASEDIR/lib -type f -name "*.zip"`
do
  CLASSPATH=$CLASSPATH:$f
done


# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

JAVA_BIN=java
LIBPATH="NONE"

case `uname -s` in 
	AIX)
		LIBPATH=$BASEDIR/libswt/aix/
		;;

	SunOS) 
		LIBPATH=$BASEDIR/libswt/solaris/
		;;

	Darwin)
		LIBPATH=$BASEDIR/libswt/osx/
		JAVA_BIN=$BASEDIR/libswt/osx/java_swt
		chmod +x $JAVA_BIN
		;;

	Linux)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				LIBPATH=$BASEDIR/libswt/linux/x86_64/
				;;

			i[3-6]86)
				LIBPATH=$BASEDIR/libswt/linux/x86/
				;;

			ppc)
				LIBPATH=$BASEDIR/libswt/linux/ppc/
				;;
            loongarch64)
                LIBPATH=$CURRENTDIR/../libswt/linux/loongarch64/
                ;;

			*)	
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;
		esac
		;;

	FreeBSD)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				LIBPATH=$BASEDIR/libswt/freebsd/x86_64/
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;

			i[3-6]86)
				LIBPATH=$BASEDIR/libswt/freebsd/x86/
				;;

			ppc)
				LIBPATH=$BASEDIR/libswt/freebsd/ppc/
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;

			*)	
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;
		esac
		;;

	HP-UX) 
		LIBPATH=$BASEDIR/libswt/hpux/
		;;
	CYGWIN*)
		./Spoon.bat
		# exit
		;;

	*) 
		echo Spoon is not supported on this hosttype : `uname -s`
		exit
		;;
esac 

export LIBPATH

if [ "$LIBPATH" != "NONE" ]
then
  for f in `find $LIBPATH -name '*.jar'`
  do
    CLASSPATH=$CLASSPATH:$f
  done
fi


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 256m to higher values in case you run out of memory.  **
# ******************************************************************

OPT="-Xmx512m -cp $CLASSPATH -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD"

# ***************
# ** Run...    **
# ***************

$JAVA_BIN $OPT org.pentaho.di.ui.i18n.editor.Translator2 "${1+$@}"

