#!/bin/sh
# -----------------------------------------------------------------------------
# Set environment variables for pentaho
#
# Calls set-pentaho-java to find PENTAHO_JAVA and PENTAHO_JAVA_HOME
# Then searches to find the pentaho installed licence file
#
# -----------------------------------------------------------------------------

setPentahoEnv() {
	DIR_REL=`dirname $0`
	cd $DIR_REL
	DIR=`pwd`
	cd -
	
	. "$DIR/set-pentaho-java.sh"
	
	if [ -d "$1" ]; then
	  setPentahoJava "$1"
	else
	  setPentahoJava
	fi
	if [ -z "$PENTAHO_INSTALLED_LICENSE_PATH" ]; then
	    if [ -f "$DIR/.installedLicenses.xml" ]; then
	      echo "DEBUG: Found Pentaho License at the current folder"
	      PENTAHO_INSTALLED_LICENSE_PATH="$DIR/.installedLicenses.xml"
	    elif [ -f "$DIR/../.installedLicenses.xml" ]; then
	      echo "DEBUG: Found Pentaho License one folder up"
	      PENTAHO_INSTALLED_LICENSE_PATH="$DIR/../.installedLicenses.xml"
	    elif [ -f "$DIR/../../.installedLicenses.xml" ]; then
	      echo "DEBUG: Found Pentaho License two folders up"
	      PENTAHO_INSTALLED_LICENSE_PATH="$DIR/../../.installedLicenses.xml"
	    fi
	fi
	echo "DEBUG: PENTAHO_INSTALLED_LICENSE_PATH=$PENTAHO_INSTALLED_LICENSE_PATH"
} 