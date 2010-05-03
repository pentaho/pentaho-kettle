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

# Fix for GTK Windows issues with SWT
export GDK_NATIVE_WINDOWS=1

# **************************************************
# ** Init BASEDIR                                 **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR

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
		echo "Starting Data Integration using 'Spoon.sh' from OS X is not supported."
		echo "Please start using 'Data Integration 32-bit' or"
		echo "'Data Integration 64-bit' as appropriate."
		exit
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

# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 256m to higher values in case you run out of memory.  **
# ******************************************************************

OPT="$OPT -Xmx256m -Xms256m -XX:MaxPermSize=128m -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"

# ***************
# ** Run...    **
# ***************
$JAVA_BIN $OPT $STARTUP -lib $LIBPATH "${1+$@}"
