#!/usr/bin/env bash

set -eo pipefail

REF_DIR=${REF:-/usr/share/jenkins/ref/plugins}

PLUGINS+=(http://nexus.pentaho.org/content/groups/omni/com/mig82/folder-properties/1.0/folder-properties-1.0.hpi)
PLUGINS+=(http://nexus.pentaho.org/content/groups/omni/org/hitachivantara/ci/plugins/pipeline-ci-utility-steps/1.8/pipeline-ci-utility-steps-1.8.hpi)

fetch() {
  local name=${1}.jpi
  local url=${2}
  # download the sha1 checksum
  curl -sSfL --connect-timeout 20 --retry 5 --retry-delay 0 --retry-max-time 60 -o ${REF_DIR}/${name}.sha1 ${url}.sha1
  # download the plugin but rename the extension as .jpi
  curl -sSfL --connect-timeout 20 --retry 5 --retry-delay 0 --retry-max-time 60 -o ${REF_DIR}/${name} ${url}
  # check integrity of downloaded files
  echo "$(cat ${REF_DIR}/${name}.sha1)  ${REF_DIR}/${name}" | sha1sum -c -
}

install() {
  local names+=()
  for plugin in "${@}"; do
    local name=${plugin%/*/*}
    name=${name##*/}
    fetch ${name} ${plugin}
    names+=(${name})
  done

  ATTEMPTS=-1 install-plugins.sh ${names[@]}
}

install ${PLUGINS[@]}
