#!/bin/bash
#
# Tool to turn eclipse directories into p2 repositories

USAGE="Usage:
  `basename "$0"` <repo-dir> [options]
  Options:
    --name <repo name>
      the repository name
    --eclipse <eclipse install dir>
      the eclipse installation to use, can also be provided as \$ECLIPSE_DIR
The <repo-dir> must contain at least a subdirectory plugins
"

fail() {
  echo -e "Error:\n  $1"
  echo "$USAGE"
  exit 1
}

if [ "$#" -lt 1 ]; then
  fail "Missing parameter <repo-dir>"
fi

repoDir=`readlink -nm "$1"`
shift
if [ ! -d "$repoDir" ]; then
  fail "Directory does not exist: $repoDir"
  exit 1
fi

repoName="Generated Repository"

while [ "$#" -gt 0 ]; do
  param="$1"
  shift
  test -z "$1" && fail "Missing parameter for '$param'"
  case "$param" in
    -n|--name)
      repoName="$1"
      shift
      ;;
    --eclipse)
      ECLIPSE_DIR="$1"
      shift
      ;;
    *)
      fail "Illegal parameter: $param"
      ;;
  esac
done

# Check Eclipse dir
if [ -z "$ECLIPSE_DIR" ]; then
  fail "Missing ECLIPSE_DIR, must point to an Eclipse installation"
fi
if [ ! -d "$ECLIPSE_DIR/plugins" ]; then
  fail "Invalid ECLIPSE_DIR: $ECLIPSE_DIR, must point to an Eclipse installation"
fi

# Find Equinox launcher
launcher=$ECLIPSE_DIR/plugins/`ls -1 $ECLIPSE_DIR/plugins 2> /dev/null | grep launcher_ | tail -n 1`
echo "Using Equinox launcher: $launcher"

# Remove existing metadata
rm -f "$repoDir"/artifacts.{xml,jar} || exit 1
rm -f "$repoDir"/content.{xml,jar} || exit 1

echo "Input directory: $repoDir"

java -cp $launcher org.eclipse.core.launcher.Main \
    -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher \
    -metadataRepository file:$repoDir \
    -artifactRepository file:$repoDir \
    -metadataRepositoryName "$repoName" \
    -artifactRepositoryName "$repoName" \
    -source "$repoDir" \
    -reusePackedFiles \
    -compress \
    -publishArtifacts \
    -consolelog \
    || exit 1
