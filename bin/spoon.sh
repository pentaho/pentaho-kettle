#!/bin/sh

# **************************************************
# ** Set these to the location of your mozilla
# ** installation directory.  Use a Mozilla with
# ** Gtk2 and Fte enabled.
# **************************************************

# set MOZILLA_FIVE_HOME=/usr/local/mozilla
# set LD_LIBRARY_PATH=/usr/local/mozilla

set MOZILLA_FIVE_HOME=/usr/lib/xulrunner-1.8.1/
set LD_LIBRARY_PATH=${MOZILLA_FIVE_HOME}:${LD_LIBRARY_PATH}
export MOZILLA_FIVE_HOME LD_LIBRARY_PATH

# **************************************************
# ** Init BASEDIR                                 **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR


# **************************************************
# ** Spoon Plugin libraries                       **
# **************************************************

PLUGINPATH=NONE

for f in `find $BASEDIR/plugins/spoon -maxdepth 2 -type d -name "lib"` 
do
if [ "$PLUGINPATH" != "NONE" ]
then
	PLUGINPATH=$PLUGINPATH:$f
else
	PLUGINPATH=$f
fi
done 

# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

JAVA_BIN=java
LIBPATH="NONE"
STARTUP="-jar launcher/launcher.jar"

case `uname -s` in 
	AIX)
		LIBPATH=$BASEDIR/../libswt/aix/
		;;

	SunOS) 
		LIBPATH=$BASEDIR/../libswt/solaris/
		;;

	Darwin)
		LIBPATH=$BASEDIR/../libswt/osx/
		JAVA_BIN=$BASEDIR/../libswt/osx/java_swt
        STARTUP=" -cp launcher.jar org.pentaho.commons.launcher.Launcher"
        OPT="-XstartOnFirstThread=true "
		chmod +x $JAVA_BIN
		;;

	Linux)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				LIBPATH=$BASEDIR/../libswt/linux/x86_64/
				;;

			i[3-6]86)
				LIBPATH=$BASEDIR/../libswt/linux/x86/
				;;

			ppc)
				LIBPATH=$BASEDIR/../libswt/linux/ppc/
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
				LIBPATH=$BASEDIR/../libswt/freebsd/x86_64/
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;

			i[3-6]86)
				LIBPATH=$BASEDIR/../libswt/freebsd/x86/
				;;

			ppc)
				LIBPATH=$BASEDIR/../libswt/freebsd/ppc/
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
		LIBPATH=$BASEDIR/../libswt/hpux/
		;;
	CYGWIN*)
		./Spoon.bat
		exit
		;;

	*) 
		echo Spoon is not supported on this hosttype : `uname -s`
		exit
		;;
esac 

export LIBPATH

# **************************************************
# ** Merge PLUGINPATH and LIBPATH into LIBSPATH   **
# **************************************************

LIBSPATH=

if [ "$LIBPATH" != "NONE" ]
then
 if [ "$PLUGINPATH" != "NONE" ]
 then
	LIBSPATH="-lib $LIBPATH:$PLUGINPATH"
 else 
	LIBSPATH="-lib $LIBPATH"
 fi 
else
 if [ "$PLUGINPATH" != "NONE" ]
 then
    LIBSPATH="-lib $PLUGINPATH"
 fi
fi

# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 256m to higher values in case you run out of memory.  **
# ******************************************************************

OPT="$OPT -Xmx256m -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"

# ***************
# ** Run...    **
# ***************
$JAVA_BIN $OPT $STARTUP $LIBSPATH "${1+$@}"
