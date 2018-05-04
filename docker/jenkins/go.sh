#!/bin/bash

set -eo pipefail

[[ -t 3 ]] || exec 3>&1 # Open file descriptor 3, writing to wherever stdout currently writes
[[ -t 4 ]] || exec 4>&2 # Open file descriptor 4, writing to wherever stderr currently writes


REPOSITORY=pentaho/jenkins
JENKINS_HOME=/var/jenkins_home
DOCKER_VOLUME=${VOLUME_NAME:-jenkins_pipeline}:${JENKINS_HOME}
SECRETS=`pwd`/secrets/credentials:/usr/share/jenkins/secrets/credentials

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

die() {
  echo -e "${RED}$1${NC}" >&4
  exit 1
}

info() {
  echo -n -e "${GREEN}$1${NC}" >&3
}

call() {
  local cmd=$@
  if [[ -n "${debug}" ]]; then
    echo "${cmd}" >&5
    return 0
  else
    $@ >&4 >&2
  fi
}

build() {
  [[ -n "${skip}" ]] && return 0
  local version=$1
  local variant=$2
  local tag="${version}${variant}"
  #local build_opts=(--no-cache --squash --pull --rm)
  local opts=("${build_opts[@]:---pull --rm}")

  build_libs_for_alpine

  info "Building Jenkins ${tag}"
  call docker build --file "Dockerfile$variant" \
    "${opts[@]+"${opts[@]}"}" \
    --tag "${REPOSITORY}:${tag}" . && \
    info " [done]\n" || die " [fail]"
}

start() {
  local version=$1
  local variant=$2
  local tag="${version}${variant}"
  local opts=("${run_opts[@]:--it --rm -v ${DOCKER_VOLUME} -v ${SECRETS}}")

  info "Starting Jenkins\n"
  call docker run \
    "${opts[@]+"${opts[@]}"}" \
    -p 8080:8080 \
    "${REPOSITORY}:${tag}"
}

build_libs_for_alpine() {
  [[ -n "${build_libs}" ]] && . alpine/tools/build.sh || return 0
}

printHelp() {
  echo "usage: ${PROGNAME} [options]"
  echo "  options:"
  echo -e "    -h, --help\t\t\t This help message"
  echo -e "    -v, --variant string\t Sets the variant of the jenkins container."
  echo -e "        --debug \t\t Enable debug mode, this will only print the commands that will be run."
  echo -e "    -q, --quiet \t\t Enable quiet mode."
  echo -e "    -d, --daemon \t\t Run in daemon mode."
  echo -e "    -n, --no-build \t\t Skip the building part."
  echo -e "    -f, --force \t\t Force building from the start."
}

while [[ $# -gt 0 ]]; do
  opt="$1"
  case "$opt" in
    -h|--help )
      printHelp
      exit 0
      ;;
    -v|--variant )
      [[ -z $2 || $2 == -* ]] && die "$opt requires an argument"
      variant="-"$2
      build_libs="ok"
      shift
      ;;
    -q|--quiet )
      exec 1>/dev/null # Redirect stdout to /dev/null, leaving fd 3 alone
      exec 2>/dev/null # Redirect stderr to /dev/null, leaving fd 4 alone
      export quiet="ok"
      ;;
    --debug )
      [[ -t 5 ]] || exec 5>&3
      exec 3>/dev/null
      export debug="ok"
      ;;
    -d|--daemon )
      run_opts=(-d --rm -v ${DOCKER_VOLUME} -v ${SECRETS})
      ;;
    -n|--no-build )
      export skip="ok"
      ;;
    -f|--force )
      build_opts=(--no-cache --pull --rm)
      export force="ok"
      ;;
    * )
      die "Unknown option: $opt"
      ;;
  esac
  shift;
done

build "lts" "$variant"
start "lts" "$variant"