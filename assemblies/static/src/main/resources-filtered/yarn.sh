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


DIR="`pwd`"

echo "$KETTLE_HOME"

if [ ! -z "$KETTLE_HOME" ] # not blank
then
	if [[ ! "$KETTLE_HOME" = /* ]] # not full path
	then
		export KETTLE_HOME="$DIR/$KETTLE_HOME"
	fi
fi

if [ ! -d "$KETTLE_HOME" ] # not found
then
	export KETTLE_HOME=
fi

echo "$KETTLE_HOME"

export IS_YARN="true"

BASEDIR="`dirname $0`"
"$BASEDIR/carte.sh" "$@"
