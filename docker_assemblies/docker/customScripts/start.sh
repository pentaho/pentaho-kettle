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
echo "Executing apply-content.sh now.  "

./apply-content.sh
##
# if there is a content-config.sh in the kettle dir, execute it, otherwise do nothing
##
KETTLE_HOME_DIR='/home/di_user/.kettle'
CONTENT_CONFIG_SCRIPT="${KETTLE_HOME_DIR}/ws-content-config.sh"

#if [[ -f ${CONTENT_CONFIG_SCRIPT} ]]
#  then
#    echo ""
#    echo "[PDI-CONTENT] ***************************************************************************************"
#    echo "[PDI-CONTENT] *** Found ${CONTENT_CONFIG_SCRIPT}, triggering its execution now.            ***"
#    echo "[PDI-CONTENT] *** WARNING: This script grants you the ability to configure PDI container at will. ***"
#    echo "[PDI-CONTENT] *** Please exercise caution! Anything done here can affect the container's health!  ***"
#    echo "[PDI-CONTENT] ***************************************************************************************"
#    ##
#    # We are sourcing the script rather than sh'ing it. The reasoning behind this comes from use cases
#    # where customers want/need the ability to tweak env. vars such as 'PENTAHO_DI_JAVA_OPTIONS' to add
#    # some extra/custom flags.
#    ##
#    . ${KETTLE_HOME_DIR}/ws-content-config.sh
#    echo "[PDI-CONTENT] ***************************************************************************************"
#    echo "[PDI-CONTENT] *** ${CONTENT_CONFIG_SCRIPT} ended execution.                                ***"
#    echo "[PDI-CONTENT] ***************************************************************************************"
#    echo ""
#fi

##
# Execute the script to unpack any plugins configuration zip files mounted in volume
##
echo "Executing content-unpack.sh now.  "
./content-unpack.sh
echo "Running tomcat..."
./catalina.sh run