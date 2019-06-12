#! /bin/bash
set -x

env | sort

echo $M2_HOME

which mvn
find /usr/local/maven-3.5.2 -type d -name conf -exec ls -ltr {} \;

ls -ltr $M2_HOME
ls -ltr $M2_HOME/conf

exit 1
