#!/bin/bash

REPOSITORY=pentaho/jenkins
JENKINS_HOME=/var/jenkins_home
DOCKER_VOLUME=jenkins_pipeline:${JENKINS_HOME}
SECRETS=`pwd`/secrets/credentials:/usr/share/jenkins/secrets/credentials

build() {
  local version=$1
  local variant=$2
  local tag="${version}${variant}"
  #local build_opts=(--no-cache --pull --rm)
  local build_opts=(--pull --rm)

  docker build --file "Dockerfile$variant" \
    "${build_opts[@]+"${build_opts[@]}"}" \
    --tag "${REPOSITORY}:${tag}" .
}

start() {
  local version=$1
  local variant=$2
  local tag="${version}${variant}"
  local run_opts=(-it --rm -v ${DOCKER_VOLUME} -v ${SECRETS})

  docker run \
    "${run_opts[@]+"${run_opts[@]}"}" \
    -p 8080:8080 \
    "${REPOSITORY}:${tag}"
}

variant=""
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -v|--variant)
        variant="-"$2
        shift
        ;;
        *)
        echo "Unknown option: $key"
        return 1
        ;;
    esac
    shift
done

build "lts" "$variant"
start "lts" "$variant"