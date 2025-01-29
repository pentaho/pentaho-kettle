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


# run all the samples in the samples/transformations directory, except "Table Output" samples

  find samples/transformations -name '*.ktr' \
| egrep -iv "Table Output" \
| while read trans
  do
    echo "EXECUTING TRANSFORMATION [$trans]"
    sh -C pan.sh -file:"$trans" -level:Minimal
  done
