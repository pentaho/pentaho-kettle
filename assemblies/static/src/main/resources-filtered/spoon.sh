#!/bin/sh

# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2005 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
#
# *****************************************************************************
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# *****************************************************************************

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

# Fix GTK 3 issues with SWT
export SWT_GTK3=0

# Fix overlay scrollbar bug with Ubuntu 11.04
export LIBOVERLAY_SCROLLBAR=0

# Fix menus not showing up on Ubuntu 14.04's unity
# Bug in: https://bugs.launchpad.net/ubuntu/+source/unity-gtk-module/+bug/1208019
export UBUNTU_MENUPROXY=0

# Supposed spoon.sh and set-env.sh files both are located in data-integration folder  
# **************************************************
# ** Set INITIALDIR, BASEDIR AND CURRENTDIR       **
# **************************************************
INITIALDIR=`pwd`
# set absolute path to data-integration folder
BASEDIR=$( cd "$( dirname "$0" )" && pwd )
CURRENTDIR="."

. "$BASEDIR/set-pentaho-env.sh"

setPentahoEnv

# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

LIBPATH="NONE"
STARTUP="$BASEDIR/launcher/launcher.jar"

if [ -z "$IS_YARN" ]; then
	# Go to directory where spoon.sh located
	cd $BASEDIR
else
	cd "$BASEDIR"
fi

case `uname -s` in 
	AIX)
	ARCH=`uname -m`
		case $ARCH in

			ppc)
				LIBPATH=$CURRENTDIR/../libswt/aix/
				;;

			ppc64)
				LIBPATH=$CURRENTDIR/../libswt/aix64/
				;;

			*)
				echo "I'm sorry, this AIX platform [$ARCH] is not yet supported!"
				exit
				;;
		esac
		;;
	SunOS)
	ARCH=`uname -m`
		case $ARCH in

			i[3-6]86)
				LIBPATH=$CURRENTDIR/../libswt/solaris-x86/
				;;

			*)
				LIBPATH=$CURRENTDIR/../libswt/solaris/
				;;
		esac
		;;

	Darwin)
    ARCH=`uname -m`
	if [ -z "$IS_KITCHEN" ]; then
		OPT="-XstartOnFirstThread $OPT"
	fi
	case $ARCH in
		x86_64)
			if $($_PENTAHO_JAVA -version 2>&1 | grep "64-Bit" > /dev/null )
                            then
			  LIBPATH=$CURRENTDIR/../libswt/osx64/
                            else
			  LIBPATH=$CURRENTDIR/../libswt/osx/
                            fi
			;;
    arm64)
        if $($_PENTAHO_JAVA -version 2>&1 | grep "version \"1\.8\..*" > /dev/null )
                              then
          echo "I'm sorry, this Mac platform [$ARCH] is not supported in Java 8"
          exit
                              else
          LIBPATH=$CURRENTDIR/../libswt/osx64_aarch/
                              fi
      ;;
		i[3-6]86)
			LIBPATH=$CURRENTDIR/../libswt/osx/
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

            if [ -f /sbin/ldconfig ]; then
              LDCONFIG=/sbin/ldconfig
            else
              LDCONFIG=ldconfig
            fi
            HASWEBKITGTK=`$LDCONFIG -p | grep webkitgtk-1.0`
            export LIBWEBKITGTK="$HASWEBKITGTK"
            export JavaScriptCoreUseJIT=0
            if [ -z "$HASWEBKITGTK" ] && [ "1" != "$SKIP_WEBKITGTK_CHECK" ]; then
              echo "#######################################################################"
              echo "WARNING:  no libwebkitgtk-1.0 detected, some features will be unavailable"
              echo "    Consider installing the package with apt-get or yum."
              echo "    e.g. 'sudo apt-get install libwebkitgtk-1.0-0'"
              echo "#######################################################################"
            fi

	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				if $($_PENTAHO_JAVA -version 2>&1 | grep "64-Bit" > /dev/null )
                                then
				  LIBPATH=$CURRENTDIR/../libswt/linux/x86_64/
                                else
				  LIBPATH=$CURRENTDIR/../libswt/linux/x86/
                                fi
				;;

			i[3-6]86)
				LIBPATH=$CURRENTDIR/../libswt/linux/x86/
				;;

			ppc)
				LIBPATH=$CURRENTDIR/../libswt/linux/ppc/
				;;

			ppc64)
				LIBPATH=$CURRENTDIR/../libswt/linux/ppc64/
				;;

			*)
				echo "I'm sorry, this Linux platform [$ARCH] is not yet supported!"
				exit
				;;
		esac
		;;

	FreeBSD)
		# note, the SWT library for linux is used, so FreeBSD should have the
		# linux compatibility packages installed
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				LIBPATH=$CURRENTDIR/../libswt/linux/x86_64/
				echo "I'm sorry, this FreeBSD platform [$ARCH] is not yet supported!"
				exit
				;;

			i[3-6]86)
				LIBPATH=$CURRENTDIR/../libswt/linux/x86/
				;;

			ppc)
				LIBPATH=$CURRENTDIR/../libswt/linux/ppc/
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
		LIBPATH=$CURRENTDIR/../libswt/hpux/
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
# ** Setup Karaf endorsed libraries directory     **
# **************************************************
JAVA_LOCALE_COMPAT=""
JAVA_ADD_OPENS=""
# required for Java 11 date/time formatting backwards compatibility
JAVA_LOCALE_COMPAT="-Djava.locale.providers=COMPAT,SPI"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.lang=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.io=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.net=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.security=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.util=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.reflect.misc=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.management/javax.management=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.management/javax.management.openmbean=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.math=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.nio=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED"

# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 2048m to higher values in case you run out of memory  **
# ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
# ******************************************************************

if [ -z "$PENTAHO_DI_JAVA_OPTIONS" ]; then
    PENTAHO_DI_JAVA_OPTIONS="-Xms1024m -Xmx2048m"
fi

OPT="$OPT $PENTAHO_DI_JAVA_OPTIONS -Djava.library.path=$LIBPATH $JAVA_LOCALE_COMPAT -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT -DKETTLE_JNDI_ROOT=$KETTLE_JNDI_ROOT"

# optional line for attaching a debugger
# OPT="$OPT -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# ***************
# ** Run...    **
# ***************
inputtoexitstatus() {
  read exitstatus
  return $exitstatus
}

if [ -n "${FILTER_GTK_WARNINGS}" ] ; then
    (((("$_PENTAHO_JAVA"  $JAVA_ADD_OPENS $OPT -jar "$STARTUP" -lib $LIBPATH "${1+$@}"  2>&1; echo $? >&3 ) | grep -viE "Gtk-WARNING|GLib-GObject|GLib-CRITICAL|^$" >&4 ) 3>&1)| inputtoexitstatus ) 4>&1
else
    "$_PENTAHO_JAVA"  $JAVA_ADD_OPENS $OPT -jar "$STARTUP" -lib $LIBPATH "${1+$@}"
fi
EXIT_CODE=$?

# return to the catalog from which spoon.sh has been started
cd "$INITIALDIR"

exit $EXIT_CODE
