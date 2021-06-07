#!/bin/bash
#
# Tool to maintain p2 composite repositories

USAGE="Usage:
  `basename "$0"` <repo-dir> [options] operation, operation ...
  Options:
    --name <repo name>
      the repository name
    --eclipse <eclipse install dir>
      the eclipse installation to use, can also be provided as \$ECLIPSE_DIR
  Operations:
    add <child>
      adds a child repository to the composite repository
      <child> can be a directory or a URL
    remove <child>
      removes a child repository to the composite repository
      <child> can be a directory or a URL

Examples:
  Create a composite repository with subfolder build-01 as first child:
    $0 /path/to/repo --name \"My repository\" add build-01
  Add childs build-05 and build-06, remove build-01:
    $0 /path/to/repo add build-05 add build-06 remove build-01
"

fail() {
  echo Composite Repository Tool
  if [ $# -gt 0 ]; then
    echo -e "Error:\n  $1"
  fi
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
fi

repoName=
addRepos=
removeRepos=

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
    add)
      addRepos="$addRepos <repository location=\"$1\" />"
      shift
      ;;
    remove)
      removeRepos="$removeRepos <repository location=\"$1\" />"
      shift
      ;;
    *)
      fail "Illegal parameter: $param"
      ;;
  esac
done

if [ -z "$addRepos" -a -z "$removeRepos" ]; then
  fail "At least one add or remove operation must be given"
fi

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

tmpfile=`mktemp`
cat > "$tmpfile" <<EOM
<?xml version="1.0" encoding="UTF-8"?>
<project name="p2 composite repository">
  <target name="default">
    <p2.composite.repository>
      <repository compressed="true" location="${repoDir}" name="${repoName}" />
      <add>
        ${addRepos}
      </add>
      <remove>
        ${removeRepos}
      </remove>
    </p2.composite.repository>
  </target>
</project>
EOM

java -cp $launcher org.eclipse.core.launcher.Main \
    -application org.eclipse.ant.core.antRunner \
    -buildfile "$tmpfile" \
    default

rm "$tmpfile"

cat > "$repoDir/p2.index" <<EOM
version=1
metadata.repository.factory.order=compositeContent.xml,\!
artifact.repository.factory.order=compositeArtifacts.xml,\!
EOM
