#!/bin/bash
#
#  HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
#
#  Copyright 2020 Hitachi Vantara. All rights reserved.
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


METASTORE_DIR='/home/di_user/.pentaho'
#mkdir "${METASTORE_DIR}"
# The location where the metastore configuration files are mounted using configmap
METASTORE_ZIP='/opt/temp/metastore.zip'

# Check if the metastore zip file exist. If it is, unzip the content to METASTORE_DIR
if [[ -f ${METASTORE_ZIP} ]]; then
  echo " Found ${METASTORE_ZIP} file. Unpacking the content"
  unzip ${METASTORE_ZIP} -d ${METASTORE_DIR}
fi

if [[ -f ${METASTORE_DIR}/AWSCredentials.properties ]]; then
  echo " Found ${METASTORE_ZIP} file. Unpacking the content"
  mv "${METASTORE_DIR}/AWSCredentials.properties" /opt/pentaho/pdi/data-integration/lib
fi
