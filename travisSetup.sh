#! /bin/bash

env | sort

echo $M2_HOME
echo ${user.home}

ls -ltr $M2_HOME/conf

ls -ltr ${user.home}/.m2

exit 1
