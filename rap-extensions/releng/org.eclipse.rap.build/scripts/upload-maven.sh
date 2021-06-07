#!/bin/bash
#
# This script is used to upload bundles to Maven Central.
# Expects pom, jar, and source jar files for upload in current working directory
# For details, see: http://wiki.eclipse.org/RAP/Maven_Central
#
# Usage:
#   upload-maven [-n|--dry-run]

URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
REPO=sonatype-nexus-staging
DRY=

fail() {
  echo ERROR: $1
  exit 1
}

while [ $# -gt 0 ]; do
  case $1 in
    -n|--dry-run) DRY=1;;
    *) fail "Illegal parameter: $1";;
  esac
  shift
done

if [ $DRY ]; then
  echo "Files to upload:"
  echo
fi

for pomfile in *.pom
do
  jarfile=${pomfile/.pom/.jar}
  sourcefile=${jarfile/_/.source_}
  test -f $pomfile || fail "POM file not found: $pomfile"
  test -f $jarfile || fail "JAR file not found: $jarfile"
  test -f $sourcefile || fail "Source JAR not found: $sourcefile"
  echo $jarfile
  if [ ! $DRY ]; then
    mvn gpg:sign-and-deploy-file -Durl=${URL} -DrepositoryId=${REPO} \
     -DpomFile=${pomfile} -Dfile=${jarfile}
  fi
  echo $sourcefile
  if [ ! $DRY ]; then
    mvn gpg:sign-and-deploy-file -Durl=${URL} -DrepositoryId=${REPO} \
     -DpomFile=${pomfile} -Dfile=${sourcefile} -Dclassifier=sources 
  fi
  echo
done

if [ ! $DRY ]; then
  echo "Done. Next steps:"
  echo "* go to https://oss.sonatype.org/ -> Staging Repositories"
  echo "* CLOSE the repository"
  echo "* download and check, then RELEASE it"
fi
