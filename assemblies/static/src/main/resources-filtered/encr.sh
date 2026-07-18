#!/bin/sh
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2030-06-15
# ******************************************************************************



BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null
java -cp "$DIR"/lib/pentaho-encryption-support-${encryption-support.version}.jar:"$DIR"/lib/jetty-util-${jetty.version}.jar:"$DIR"/classes org.pentaho.support.encryption.Encr "$@"

