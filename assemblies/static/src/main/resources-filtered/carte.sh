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


# OS specific support.
darwin=false;
case "$(uname -s)" in
  Darwin*)
    darwin=true
    ;;
esac

BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null
OPT="$OPT -Dorg.mortbay.util.URI.charset=UTF-8"
if [ ! "x$JAAS_LOGIN_MODULE_CONFIG" = "x" -a ! "x$JAAS_LOGIN_MODULE_NAME" = "x" ]; then
	OPT=$OPT" -Djava.security.auth.login.config=$JAAS_LOGIN_MODULE_CONFIG"
	OPT=$OPT" -Dloginmodulename=$JAAS_LOGIN_MODULE_NAME"
fi
if [ "${darwin}" = "true" ]; then
  OPT="$OPT -Djava.awt.headless=true"
fi
export OPT
"$DIR/spoon.sh" -main org.pentaho.di.www.Carte "$@"
