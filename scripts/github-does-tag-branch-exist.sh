#!/bin/bash

branch_or_tag_check() {
  REPO_URL=$1
  TAG_TO_CHECK=$2
  echo -n "Checking ${REPO_URL} for ${TAG_TO_CHECK}..."
  TAG_CHECK=$(git ls-remote ${REPO_URL} ${TAG_TO_CHECK} 2>/dev/null)
  if [ -z "${TAG_CHECK}" ]; then
    echo " DOES NOT EXIST!"
  else
    echo " Exists"
  fi
}

if [ -z "$1" ]; then
  echo "USAGE: $0 <branch or tag to check for existence>"
  exit 0
fi

# Grab the Git URL from column 2 of github-projects.csv and send to tagcheck
TAG_OR_BRANCH_CHECK=$1
REPOS_TO_CHECK=$(cat ../../github-projects.csv | cut -d ',' -f2)
while read line; do branch_or_tag_check "$line" "$TAG_OR_BRANCH_CHECK"; done <<< "$REPOS_TO_CHECK"
