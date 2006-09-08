#!/bin/sh

# run all the samples in the samples/transformations directory, except "Table Output" samples

  find samples/transformations -name '*.ktr' \
| egrep -iv "Table Output" \
| while read trans
  do
    echo "EXECUTING TRANSFORMATION [$trans]"
    sh -C pan.sh -file:"$trans" -level:Minimal
  done
