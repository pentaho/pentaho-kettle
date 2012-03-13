#!/bin/sh

# **************************************************
# ** Set these to the location of your mozilla
# ** installation directory.  Use a Mozilla with
# ** Gtk2 and Fte enabled.
# **************************************************

# set MOZILLA_FIVE_HOME=/usr/local/mozilla
# set LD_LIBRARY_PATH=/usr/local/mozilla

# Try to guess xulrunner location - change this if you need to
MOZILLA_FIVE_HOME=$(find /usr/lib -maxdepth 1 -name xulrunner-[0-9]* | head -1)
LD_LIBRARY_PATH=${MOZILLA_FIVE_HOME}:${LD_LIBRARY_PATH}
export MOZILLA_FIVE_HOME LD_LIBRARY_PATH

# Fix for GTK Windows issues with SWT
export GDK_NATIVE_WINDOWS=1

# Fix overlay scrollbar bug with Ubuntu 11.04
export LIBOVERLAY_SCROLLBAR=0

# **************************************************
# ** Init BASEDIR                                 **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR
DIR=`pwd`
cd -

. "$DIR/set-pentaho-env.sh"

setPentahoEnv

# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

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
    ARCH=`uname -m`
	OPT="-XstartOnFirstThread $OPT"
	case $ARCH in
		x86_64)
			if $($_PENTAHO_JAVA -version 2>&1 | grep "64-Bit" > /dev/null )
                            then
			  LIBPATH=$BASEDIR/../libswt/osx64/
                            else
			  LIBPATH=$BASEDIR/../libswt/osx/
                            fi
			;;

		i[3-6]86)
			LIBPATH=$BASEDIR/../libswt/osx/
			;;

		*)	
			echo "I'm sorry, this Mac platform [$ARCH] is not yet supported!"
			echo "Please try starting using 'Data Integration 32-bit' or"
			echo "'Data Integration 64-bit' as appropriate."
			exit
			;;
	esac
	;;


	Linux)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				if $($_PENTAHO_JAVA -version 2>&1 | grep "64-Bit" > /dev/null )
                                then
				  LIBPATH=$BASEDIR/../libswt/linux/x86_64/
                                else
				  LIBPATH=$BASEDIR/../libswt/linux/x86/
                                fi
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
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
				exit
				;;

			i[3-6]86)
				LIBPATH=$BASEDIR/../libswt/freebsd/x86/
				;;

			ppc)
				LIBPATH=$BASEDIR/../libswt/freebsd/ppc/
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
				exit
				;;

			*)	
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
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
# ** Change 512m to higher values in case you run out of memory   **
# ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
# ******************************************************************

if [ -z "$PENTAHO_DI_JAVA_OPTIONS" ]; then
    PENTAHO_DI_JAVA_OPTIONS="-Xmx512m -XX:MaxPermSize=256m"
fi

OPT="$OPT $PENTAHO_DI_JAVA_OPTIONS -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"

# ***************
# ** Run...    **
# ***************
"$_PENTAHO_JAVA" $OPT $STARTUP -lib $LIBPATH "${1+$@}"
