#!/usr/bin/env bash

set -eo pipefail

[[ -t 3 ]] || exec 3>&1 # Open file descriptor 3, writing to wherever stdout currently writes
[[ -t 4 ]] || exec 4>&2 # Open file descriptor 4, writing to wherever stderr currently writes

PROGNAME=$(basename "$0")

unameOut="$(uname -s)"
case "${unameOut}" in
    MINGW*)    pwdcmd="pwd -W";;
    *)         pwdcmd="pwd";;
esac

REPOSITORY=pentaho/jenkins
JENKINS_HOME=/var/jenkins_home
CREDENTIALS_PATH=/usr/share/jenkins/secrets/credentials
DOCKER_VOLUME=${VOLUME_NAME:=jenkins_pipeline}:${JENKINS_HOME}
SECRETS=${CREDENTIALS:=`${pwdcmd}`/secrets/credentials}:${CREDENTIALS_PATH}

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

realpath() {
  OURPWD=`${pwdcmd}`
  cd "$(dirname "${1}")"
  LINK=$(readlink "$(basename "${1}")")
  while [ "${LINK}" ]; do
    cd "$(dirname "${LINK}")"
    LINK=$(readlink "$(basename "${1}")")
  done
  REALPATH="`${pwdcmd}`/$(basename "${1}")"
  cd "${OURPWD}"
  echo "${REALPATH}"
}

die() {
  echo -e "${RED}$1${NC}" >&4
  exit 1
}

info() {
  echo -n -e "${GREEN}$1${NC}" >&3
}

call() {
  [[ -z ${quiet} ]] && info "\n"
  if [[ -n "${debug}" ]]; then
    local cmd=$(echo -n "${@/%/$'\n'}" | sed 's/$/ \\/')
    echo -e "${cmd%?}\n" >&5
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
  build_opts+=(--pull --rm)

  build_libs_for_alpine

  info "Building Jenkins ${tag}"
  call "docker build" "--file Dockerfile$variant" \
    "${build_opts[@]+"${build_opts[@]}"}" \
    "--tag ${REPOSITORY}:${tag}" . && \
    info " [done]\n" || die " [fail]"
}

start() {
  [[ -n "${build}" ]] && return 0
  local version=$1
  local variant=$2
  local tag="${version}${variant}"
  run_opts+=(-i --rm)
  volumes+=("-v ${DOCKER_VOLUME}" "-v ${SECRETS}")

  info "Starting Jenkins\n"
  call "docker run" \
    "${run_opts[@]+"${run_opts[@]}"}" \
    "${volumes[@]+"${volumes[@]}"}" \
    "-p 8080:8080" \
    "${REPOSITORY}:${tag}"
}

build_libs_for_alpine() {
  [[ -n "${build_libs}" ]] && . alpine/tools/build.sh || return 0
}

setSecrets() {
  SECRETS=${CREDENTIALS}:${CREDENTIALS_PATH}
}

printHelp() {
  echo "usage: ${PROGNAME} [options]"
  echo "  options:"
  echo -e "    -h, --help\t\t\t This help message"
  echo -e "    -v, --variant string\t Sets the variant of the jenkins container."
  echo -e "        --debug \t\t Enable debug mode, this will only print the commands that will be issued."
  echo -e "    -q, --quiet \t\t Enable quiet mode."
  echo -e "    -d, --daemon \t\t Run in daemon mode."
  echo -e "    -n, --no-build \t\t Skip the building part."
  echo -e "    -b, --build \t\t Skip the run part."
  echo -e "    -f, --force \t\t Force building from the start."
  echo -e "    -c, --credentials file \t Use this file as credentials."
  echo -e "        --env-file file \t Use this file to set the container's environment variables."
  echo -e "        --bind-mount string \t Mount a file or directory into the container."
  echo -e "                            \t example: <host-file>:<destination>"
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
      run_opts+=(-d)
      ;;
    -n|--no-build )
      export skip="ok"
      ;;
    -b|--build )
      export build="ok"
      ;;
    -f|--force )
      build_opts+=(--no-cache)
      export force="ok"
      ;;
    -c|--credentials )
      [[ -z $2 || $2 == -* ]] && die "$opt requires an argument"
      CREDENTIALS=$(realpath $2)
      [[ -f ${CREDENTIALS} ]] || die "$2: No such file or directory"
      setSecrets
      shift
      ;;
    --env-file )
      [[ -z $2 || $2 == -* ]] && die "$opt requires an argument"
      env_list=$(realpath $2)
      [[ -f ${env_list} ]] || die "$2: No such file or directory"
      run_opts+=("--env-file ${env_list}")
      shift
      ;;
    --bind-mount )
      [[ -z $2 || $2 == -* ]] && die "$opt requires an argument"
      IFS=':' read -ra mount <<< "${2}"
      [[ "${#mount[@]}" -ge 2 ]] || die "Invalid mount point ${mount[*]}.\nUsage is <src>:<dst>"
      mount[0]=$(realpath ${mount[0]})
      printf -v var "%s:" "${mount[@]}"
      volumes+=("-v ${var%?}")
      shift
      ;;
    * )
      die "Unknown option: $opt"
      ;;
  esac
  shift;
done

build "lts" "$variant"
start "lts" "$variant"