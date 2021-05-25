#!/bin/bash
#
#  HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
#
#  Copyright 2017 Hitachi Vantara. All rights reserved.
#
#  NOTICE: All information including source code contained herein is, and
#  remains the sole property of Hitachi Vantara and its licensors. The intellectual
#  and technical concepts contained herein are proprietary and confidential
#  to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
#  patents, or patents in process, and are protected by trade secret and
#  copyright laws. The receipt or possession of this source code and/or related
#  information does not convey or imply any rights to reproduce, disclose or
#  distribute its contents, or to manufacture, use, or sell anything that it
#  may describe, in whole or in part. Any reproduction, modification, distribution,
#  or public display of this information without the express written authorization
#  from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
#  international treaties. Access to the source code contained herein is strictly
#  prohibited to anyone except those individuals and entities who have executed
#  confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
#  explicitly covering such access.
#

WORK_DIR=$(pwd)
DI_DIR='/usr/local/tomcat/webapps/spoon'
KETTLE_HOME_DIR='/home/di_user/.kettle'

CONTENT_CONFIG_FILE='ws-content-config.properties'
DEFAULT_CONTENT_CONFIG_FILE='default-content-config.properties'

CONFIG_FILE="${KETTLE_HOME_DIR}/${CONTENT_CONFIG_FILE}"

if [[ ! -f ${CONFIG_FILE} ]]; then
  # fallback to default-content-config.properties
  echo "[PDI-CONTENT] Could not find a ${CONTENT_CONFIG_FILE} on '.kettle' volume, using default configuration."
  CONFIG_FILE="${WORK_DIR}/${DEFAULT_CONTENT_CONFIG_FILE}"
fi


# Go through all configured files in content-config.properties and copy them to their respective directory
if [[ -f ${CONFIG_FILE} ]]; then
  while IFS='=' read -r key value
  do
    if [[ ! -z "${key}" ]] && [[ ! -z "${value}" ]] && [[ ! ${key} = \#* ]]; then
      src=$(eval echo "${key}")
      dest=$(eval echo "${value}")
      echo "[PDI-CONTENT] Applying content from ${src} to ${dest}"

      cp -rf ${src} ${dest}
    fi
  done < "${CONFIG_FILE}"
else
  echo "[PDI-CONTENT] Could not find any content configuration, skipping applying content."
fi

