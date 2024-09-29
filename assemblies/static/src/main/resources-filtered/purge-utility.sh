#!/bin/sh

# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2006 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
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

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
#cd -

. "$DIR/set-pentaho-env.sh"
setPentahoEnv

JAVA_ADD_OPENS=""
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.lang=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/java.net=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
JAVA_ADD_OPENS="$JAVA_ADD_OPENS --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"

# uses Java 6 classpath wildcards
# quotes required around classpath to prevent shell expansion
"$_PENTAHO_JAVA" $JAVA_ADD_OPENS -Xmx2048m -classpath "$DIR/plugins/pdi-pur-plugin/*:$DIR/lib/*:$DIR/classes" com.pentaho.di.purge.RepositoryCleanupUtil "$@"
