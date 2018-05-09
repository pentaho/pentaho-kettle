#!/usr/bin/env bash

set -eo pipefail

[[ -n "${skip}" ]] && exit 0

[[ -t 3 ]] || exec 3>&1
[[ -t 4 ]] || exec 4>&2


RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# lets look were are and move to the directory that contains the Dockerfile.phantomjs
DOCKERNAME="Dockerfile.phantomjs"
DOCKERFILE=$(find `pwd` -type f -name ${DOCKERNAME} -print -quit)
DOCKERFILE_PATH=$(dirname ${DOCKERFILE})
FILENAME="dockerized-phantomjs.tgz"

build() {
  info "Building phantomjs shared libs"
  call docker build --rm -t phantomjslibs --file ${DOCKERNAME} . || die " [fail]"
  call docker run --name phantomjslibs -e PHANTOMJS_VERSION=2.1.1 phantomjslibs || die " [fail]"
  call docker cp phantomjslibs:/dockerized-phantomjs.tgz ./${FILENAME} || die " [fail]"
  call docker rm phantomjslibs || die " [fail]"
  info " [done]\n"
}

die() {
  echo -e "${RED}$1${NC}" >&4
  exit 1
}

info() {
  echo -n -e "${GREEN}$1${NC}" >&3
}

call() {
  local cmd=$@
  [[ -z ${quiet} ]] && info "\n"
  if [[ -n "${debug}" ]]; then
    echo "${cmd}" >&5
    return 0
  else
    $@ >&4 >&2
  fi
}

## is debug enabled?
if [[ -n "${debug}" ]]; then
  [[ -t 5 ]] || exec 5>&3
  exec 3>/dev/null
fi
if [[ -n "${quiet}" ]]; then
  exec 1>/dev/null
  exec 2>/dev/null
fi

pushd ${DOCKERFILE_PATH}
# skip if file exists to avoid unnecessary work, but let debug print the commands
if [[ -f ${FILENAME} && -z ${force} ]] && [[ -z "${debug}" ]]; then
  info "File \"${FILENAME}\" exists. Skipping build!\n"
else
  build
fi
popd
