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

# run all the samples in the samples/transformations directory, except "Table Output" samples

  find samples/transformations -name '*.ktr' \
| egrep -iv "Table Output" \
| while read trans
  do
    echo "EXECUTING TRANSFORMATION [$trans]"
    sh -C pan.sh -file:"$trans" -level:Minimal
  done
