#!/bin/bash
set -e
# This script works by copying the hosted files to FTP Golden
#
# The transfer mechanism to FTP Golden is via a copy on the build node to a NFS mount
#
# The transfer mechanism to Box is lftp (http://lftp.tech/lftp-man.html) using these
# settings:
#
# - set net:timeout 25 = timeout after 25 seconds
# - set ftp:ssl-allow no = don't fail if the site doesn't have a cert.
# - mirror -I <glob> -n -R = mirror the glob pattern up (-R), only replacing newer files (-n)

BOX_FTP_HOST_NAME=ftp.box.com
GOLDEN_FTP_HOST_NAME=ftpgolden.pentaho.net
PROMOTION_LOG=promotion_log.txt

#
# Helper functions
#
function error_exit {
    echo "ERROR: $1"
    exit "${2:-1}" # Param 2 can be an exit code, otherwise exit with 1
}

# Write string(s) to promotion log
#    $1 = string $2..$X = params to printf format string
function write_to_promotion_log {
  printf "$1" "${@:2}" 2>&1 | tee -a ${PROMOTION_LOG}
}

# Write the files to the manifest found in the folder
#    $1 = folder
function write_manifest_to_promotion_log {
  cat "$1/release-manifest.txt" | tee -a ${PROMOTION_LOG}
}

# Copy files to FTP Golden
#    $1 = source folder, $2 = destination folder
function promote_to_ftp_golden {
  if [ "${PROMOTE_TO_FTP_GOLDEN}" == "true" ]; then
    rsync -av -L -K --progress --exclude not-for-release "$1/." "$2"
  fi
}

# Upload/Mirror files to Box via the glob
#    $1 = source folder, $3 = destination folder
# TODO: Using the BOX API, do a folder copy to the releases folder
function promote_to_box {
  if [ "${PROMOTE_TO_BOX}" == "true" ]; then
    box_shared_link=$(python3 box_folder_copy.py "${BOXGUY}" "${1}" "${2}" "${3}")
  fi  
}

# Write job params to manifest:
write_to_promotion_log "DEPLOYMENT_FOLDER: %s\n"                   "${DEPLOYMENT_FOLDER}"
write_to_promotion_log "BUILD_NUMBER: %s\n"                        "${BUILD_NUMBER}"
write_to_promotion_log "RELEASE_TYPE: %s\n"                        "${RELEASE_TYPE}"
write_to_promotion_log "SHIM_TYPE: %s\n"                           "${SHIM_TYPE}"
write_to_promotion_log "BRANCH: %s\n"                              "${BRANCH}"
write_to_promotion_log "PROMOTE_TO_BOX: %s\n"                      "${PROMOTE_TO_BOX}"
write_to_promotion_log "PROMOTE_TO_FTP_GOLDEN: %s\n"               "${PROMOTE_TO_FTP_GOLDEN}"
write_to_promotion_log "EMAIL_RECIPIENTS: %s\n"                    "${EMAIL_RECIPIENTS}"
write_to_promotion_log "BUILD_HOSTING_ROOT: %s\n"                  "${BUILD_HOSTING_ROOT}"
write_to_promotion_log "SHIM_GOLDEN_BASE_DIR: %s\n"                "${SHIM_GOLDEN_BASE_DIR}"
write_to_promotion_log "SHIM_BOX_BASE_DIR: %s\n"                   "${SHIM_BOX_BASE_DIR}"
write_to_promotion_log "SUITE_GOLDEN_BASE_DIR: %s\n"               "${SUITE_GOLDEN_BASE_DIR}"
write_to_promotion_log "SUITE_BOX_BASE_DIR: %s\n"                  "${SUITE_BOX_BASE_DIR}"

HOSTED_RELEASE_BASE_DIR=${BUILD_HOSTING_ROOT}/${DEPLOYMENT_FOLDER}/${BUILD_NUMBER}/release
write_to_promotion_log "HOSTED_RELEASE_BASE_DIR: %s\n\n" "${HOSTED_RELEASE_BASE_DIR}"


BOX_SOURCE_DIR="CI/${DEPLOYMENT_FOLDER}/${BUILD_NUMBER}/"    
write_to_promotion_log "BOX_SOURCE_DIR: %s\n\n" "${BOX_SOURCE_DIR}"

# Check parameters
if [ -z "${RELEASE_TYPE}" ]; then
   error_exit "RELEASE_TYPE is unspecified."
fi

if [ "${RELEASE_TYPE}" == "Shim" ] && [ -z "${SHIM_TYPE}" ]; then
   error_exit "RELEASE_TYPE is 'Shim', but no SHIM_TYPE was specified."
fi

# Error if root folders do not exist
if [ ! -d "${BUILD_HOSTING_ROOT}" ]; then
   error_exit "BUILD_HOSTING_ROOT does not exist: ${BUILD_HOSTING_ROOT}"
fi

if [ ! -d "${HOSTED_RELEASE_BASE_DIR}" ]; then
   error_exit "HOSTED_RELEASE_BASE_DIR does not exist: ${HOSTED_RELEASE_BASE_DIR}"
fi

# Shim promotion setup
if [ "${RELEASE_TYPE}" == "Shim" ]; then
  GOLDEN_RELEASE_BASE_DIR=${SHIM_GOLDEN_BASE_DIR}/${SHIM_TYPE}/${DEPLOYMENT_FOLDER}
  BOX_RELEASE_BASE_DIR=${SHIM_BOX_BASE_DIR}/${SHIM_TYPE}
fi
# Suite promotion setup
if [ "${RELEASE_TYPE}" == "Suite" ]; then
  GOLDEN_RELEASE_BASE_DIR=${SUITE_GOLDEN_BASE_DIR}/${DEPLOYMENT_FOLDER}
  BOX_RELEASE_BASE_DIR=${SUITE_BOX_BASE_DIR}
fi

# Do the actual promotion
mkdir -p ${GOLDEN_RELEASE_BASE_DIR}
promote_to_ftp_golden ${HOSTED_RELEASE_BASE_DIR} "${GOLDEN_RELEASE_BASE_DIR}/"
promote_to_box ${BOX_SOURCE_DIR} "${BOX_RELEASE_BASE_DIR}/" "${DEPLOYMENT_FOLDER}"

# Write the email body to the promotion log for easy cut and paste, eventually this
# should be emailed directly to the consumers
write_to_promotion_log "\n"
write_to_promotion_log "${DEPLOYMENT_FOLDER}-${BUILD_NUMBER} has been promoted:\n"

# Write the
if [ "${PROMOTE_TO_BOX}" == "true" ]; then
  write_to_promotion_log "\nBox: ${box_shared_link}"
  write_to_promotion_log "\nBox FTP: ftp://%s/%s" "${BOX_FTP_HOST_NAME}" "${BOX_RELEASE_BASE_DIR}"
fi
if [ "${PROMOTE_TO_FTP_GOLDEN}" == "true" ]; then
  # Remove "/build/PentahoNightly/" prefix
  GOLDEN_FTP_BASE_DIR=${GOLDEN_RELEASE_BASE_DIR#/build/PentahoNightly/}
  write_to_promotion_log "\nGolden: ftp://%s/%s\n" "${GOLDEN_FTP_HOST_NAME}" "${GOLDEN_FTP_BASE_DIR}"
fi

write_to_promotion_log "\n"

# Write out the release manifest to the promotion log
if [ "${RELEASE_TYPE}" == "Shim" ]; then
  write_to_promotion_log "%s %s Shim release manifest:\n\n"  "${DEPLOYMENT_FOLDER}" "${SHIM_TYPE}"
fi
if [ "${RELEASE_TYPE}" == "Suite" ]; then
  write_to_promotion_log "%s Suite release manifest:\n\n"  "${DEPLOYMENT_FOLDER}"
fi

write_manifest_to_promotion_log ${HOSTED_RELEASE_BASE_DIR}
