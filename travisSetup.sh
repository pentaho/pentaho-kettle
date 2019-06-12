#! /bin/bash
set -x

env | sort

echo $M2_HOME

which mvn

ls -ltr $M2_HOME
ls -ltr $M2_HOME/conf

exit 1
