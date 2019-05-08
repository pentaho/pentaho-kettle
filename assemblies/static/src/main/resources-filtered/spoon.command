# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2005 - ${copyright.year} by Hitachi Vantara : http://www.pentaho.com
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

cd `dirname $0`

# if a BASE_DIR argument has been passed to this .command, use it
if [ -n "$1" ] && [ -d "$1" ] && [ -x "$1" ]; then
    echo "DEBUG: Using value ($1) from calling script"
    cd "$1"
fi

./spoon.sh
exit
