#!/bin/sh

# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2014 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
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

echo "SpoonDebug is to support you in finding unusual errors and start problems."
echo "-"

echo "Set logging level to Debug? (default: Basic logging)"
echo "Debug? (Y=Yes, N=No, C=Cancel)"
while true ; do
    read ync
    case $ync in
        Y* ) SPOON_OPTIONS="-level=Debug";export SPOON_OPTIONS;break;;
        N* ) break;;
        C* ) exit;;
        * ) exit;;
    esac
done

echo "-"
echo "Redirect console output to SpoonDebug.txt in the actual Spoon directory?"
echo "Redirect to SpoonDebug.txt? (Y=Yes, N=No, C=Cancel)"
while true ; do
    read ync
    case $ync in
        Y* ) SPOON_REDIRECT="1";unset SPOON_PAUSE;break;;
        N* ) break;;
        C* ) exit;;
        * ) exit;;
    esac
done

echo "-"
SPOONDIR=$(dirname "$(readlink -f "$0")")
echo Launching Spoon: "$SPOONDIR/spoon.sh $SPOON_OPTIONS"
if [ "$SPOON_REDIRECT" != "1" ] ; then
    exec ./spoon.sh $SPOON_OPTIONS
else
    echo "Console output gets redirected to $SPOONDIR/SpoonDebug.txt"
    exec ./spoon.sh $SPOON_OPTIONS >> "$SPOONDIR/SpoonDebug.txt" 2>&1
fi
