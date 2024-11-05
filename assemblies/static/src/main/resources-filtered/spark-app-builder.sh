#!/bin/sh

# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2017 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
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

INITIALDIR="`pwd`"
BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null

if [ -z "$PENTAHO_DI_JAVA_OPTIONS" ]; then
	export PENTAHO_DI_JAVA_OPTIONS="-Xms1024m -Xmx3072m"
fi

"$DIR/spoon.sh" -main org.pentaho.pdi.spark.driver.app.builder.SparkDriverAppBuilder "$@"
