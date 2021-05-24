#!/bin/bash
#
# This script executes the PDE antRunner with the given parameters
# Usage example:
# ant-runner.sh -buildfile comp-repo.xml -DrepoDir=$repoDir target

if [ $# == 0 ]; then
  echo "Missing parameters"
  echo "Usage:"
  echo "  ant-runner.sh <buildfile> [parameters]"
  echo "Example:"
  echo "  ant-runner.sh build.xml -DrepoDir=\"\$repoDir\" target"
  exit 0
fi

if [ -z "$JAVA_HOME" ]; then
  echo "Missing JAVA_HOME"
  exit 1
elif [ ! -f "$JAVA_HOME/bin/java" ]; then
  echo "Invalid JAVA_HOME: '$JAVA_HOME'"
  exit 1
fi

java=$JAVA_HOME/bin/java

if [ -z "$ECLIPSE_HOME" ]; then
  echo "Missing ECLIPSE_HOME"
  exit 1
elif [ ! -d "$ECLIPSE_HOME/plugins" ]; then
  echo "Invalid ECLIPSE_HOME: '$ECLIPSE_HOME'"
  exit 1
fi

# Find Equinox launcher
launcher=$(ls -1 $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_*.jar | tail -n 1)

if [ -z "$launcher" ]; then
  echo "No Equinox launcher found"
  exit 1
elif [ ! -f "$launcher" ]; then
  echo "Invalid Equinox launcher: '$launcher'"
  exit 1
fi

echo "Using Equinox launcher: $launcher"

buildfile=$1
shift

exec $java -cp $launcher org.eclipse.core.launcher.Main \
  -application org.eclipse.ant.core.antRunner \
  -buildfile $buildfile \
  "$@"

