#!/bin/bash

# author: Steve Maring <steve.maring@hitachivantara.com>
# created: JUL 7, 2022

# This script will upload the SRC_PATH to pentaho.box.com.  The directory layout will vary slightly depending
# on if it is a regular upload with a BUILD_NUM or a RELEASE.

# execution:

# $ ./upload-to-box.sh \
#     <BOX_APP_CLIENT_ID> \
#     <BOX_APP_CLIENT_SECRET> \
#     <SRC_PATH> \
#     <VERSION> \
#     <BUILD_NUM> \     # build number or RELEASE if doing a release promotion
#     <PARENT_FOLDER_NAME> \
#     <DEBUG_ENABLED>   # true or false

# example of a nightly QAT build ...
# $ ./upload-to-box.sh \
#     rhelq1mba3umg1ii8mtiv9j8d3tsq577 \
#     ************************** \
#     /build2/artifacts/hosted/9.4-QAT/129/release \
#     9.4-QAT \
#     129 \
#     /CI \
#     false

# example of an SP build ...
# $ ./upload-to-box.sh \
#     rhelq1mba3umg1ii8mtiv9j8d3tsq577 \
#     ************************** \
#     /build2/artifacts/hosted/9.2.0.4/591/release \
#     9.2.0.4 \
#     591 \
#     /CI \
#     false

# example of an SP/Release promotion ...
# $ ./upload-to-box.sh \
#     rhelq1mba3umg1ii8mtiv9j8d3tsq577 \
#     ************************** \
#     /build2/artifacts/hosted/9.2.0.4/591/release \
#     9.2.0.4 \
#     RELEASE \
#     /CI/9.2-Releases \
#     false


if [ "$#" -ne 7 ]; then 
  echo "Missing arguments to script.  Format is ..."
  echo " $ ./upload-to-box.sh \ "
  echo "     <BOX_APP_CLIENT_ID> \ "
  echo "     <BOX_APP_CLIENT_SECRET> \ "
  echo "     <SRC_PATH> \ "
  echo "     <VERSION> \ "
  echo "     <BUILD_NUM> \ "
  echo "     <PARENT_FOLDER_NAME> \ "
  echo "     <DEBUG_ENABLED>"
  
  exit 1
fi

# check Box.com for the folder ID of new folders.  They show in the URL of the folder
# /CI = 261814384
# /CI/8.3-Releases = 79406720943
# /CI/9.2-Releases = 124004124097
# /CI/9.3-Releases = 160972574326
# /CI/9.4-Releases = 167049477557

declare -A FOLDER_ID_MAP

FOLDER_ID_MAP["/CI"]=261814384
FOLDER_ID_MAP["/CI/8.3-Releases"]=79406720943
FOLDER_ID_MAP["/CI/9.2-Releases"]=124004124097
FOLDER_ID_MAP["/CI/9.3-Releases"]=160972574326
FOLDER_ID_MAP["/CI/9.4-Releases"]=167049477557

# for testing
FOLDER_ID_MAP["/buildguy"]=138002148
FOLDER_ID_MAP["/buildguy/upload-test"]=166411442411


BOX_APP_CLIENT_ID=$1
BOX_APP_CLIENT_SECRET=$2
SRC_PATH=$3
VERSION=$4
BUILD_NUM=$5
PARENT_FOLDER_NAME=$6
DEBUG_ENABLED=$7

IS_DEBUG_ENABLED="$DEBUG_ENABLED"

echo "BOX_APP_CLIENT_ID: $BOX_APP_CLIENT_ID"
echo "BOX_APP_CLIENT_SECRET: $BOX_APP_CLIENT_SECRET"
echo "SRC_PATH: $SRC_PATH"
echo "VERSION: $VERSION"
echo "BUILD_NUM: $BUILD_NUM"
echo "PARENT_FOLDER_NAME: $PARENT_FOLDER_NAME"
echo "DEBUG_ENABLED: $DEBUG_ENABLED"


get_access_token () {
  echo "obtaining access token ..."
  COMMAND="curl -s -X POST \"https://api.box.com/oauth2/token\" \
      -H \"Content-Type: application/x-www-form-urlencoded\" \
      -d \"client_id=$BOX_APP_CLIENT_ID\" \
      -d \"client_secret=$BOX_APP_CLIENT_SECRET\" \
      -d \"grant_type=client_credentials\" \
      -d \"box_subject_type=enterprise\"  \
      -d \"box_subject_id=80272\" \
      | jq '.'"
  RESPONSE=$(eval "${COMMAND} 2>&1")
  RET=$?
  TOKEN_FAILURE=false
  if [ $RET -ne 0 ]; then
    TOKEN_FAILURE=true
  fi
  if [ "$TOKEN_FAILURE" == "true" ] || [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "${COMMAND}"
  fi
  if [ "$TOKEN_FAILURE" == "true" ] || [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "${RESPONSE}"
  fi
  ACCESS_TOKEN=$(echo "${RESPONSE}" | jq '.access_token')
  if [ "${ACCESS_TOKEN}" == *"error"* ]; then
    echo "${RESPONSE}"
    exit 1;
  fi
}


get_folder_id () {
  PARENT_ID="$1"
  TARGET_FOLDER_PATH="$2"
  TARGET_FOLDER_PATH_ARRAY=($(echo $TARGET_FOLDER_PATH | sed "s/\// /g"))
  TARGET_FOLDER_NAME=${TARGET_FOLDER_PATH_ARRAY[-1]}

  if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "getting folder ID for $TARGET_FOLDER_PATH under $PARENT_ID ..."
  fi

  if [[ ! -z "${FOLDER_ID_MAP[$TARGET_FOLDER_PATH]}" ]]; then
    if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
      echo "folder already in map"
    fi
    FOLDER_ID="${FOLDER_ID_MAP[$TARGET_FOLDER_PATH]}"
  else
    echo "trying to create folder ..."
    COMMAND="curl -s -X POST \"https://api.box.com/2.0/folders\" \
                  -H \"Authorization: Bearer $ACCESS_TOKEN\" \
                  -H \"Content-Type: application/json\" \
                  -d '{
                    \"name\": \"$TARGET_FOLDER_NAME\",
                    \"parent\": {
                      \"id\": \"$PARENT_ID\"
                    }
                  }' \
                  | jq '.'"
    
    RESPONSE=$(eval "${COMMAND} 2>&1")
    RET=$?
    FOLDER_FAILURE=false
    if [ $RET -ne 0 ]; then
      FOLDER_FAILURE=true
    fi
    if [ "$FOLDER_FAILURE" == "true" ] || [ "${IS_DEBUG_ENABLED}" == "true" ]; then
      echo "${COMMAND}"
    fi
    if [ "$FOLDER_FAILURE" == "true" ] || [ "${IS_DEBUG_ENABLED}" == "true" ]; then
      echo "${RESPONSE}"
    fi
    if [ "$(echo ${RESPONSE} | jq -r '.code')" == "item_name_in_use" ]; then
      echo "directory seems to already exist"
      FOLDER_ID=$(echo ${RESPONSE} | jq -r '.context_info.conflicts[0].id')
    else
      echo "directory created successfully"
      FOLDER_ID=$(echo ${RESPONSE} | jq -r '.id')
    fi
    FOLDER_ID_MAP[$TARGET_FOLDER_PATH]=$FOLDER_ID
  fi
}


upload_file () {
  PARENT_ID="$1"
  FILE_PATH="$2"

  FILE_PATH_ARRAY=($(echo $FILE_PATH | sed "s/\// /g"))
  FILE_NAME=${FILE_PATH_ARRAY[-1]}

  echo "uploading $FILE_PATH ..."
  COMMAND="curl -s --retry 3 -X POST \"https://upload.box.com/api/2.0/files/content\" \
                -H \"Authorization: Bearer $ACCESS_TOKEN\" \
                -H \"Content-Type: multipart/form-data\" \
                -F attributes='{\"name\":\"$FILE_NAME\", \"parent\":{\"id\":\"$PARENT_ID\"}}' \
                -F file=@$FILE_PATH "
    
  RESPONSE=$(eval "${COMMAND} 2>&1")
  RET=$?
  UPLOAD_FAILURE=false
  if [ $RET -ne 0 ]; then
    UPLOAD_FAILURE=true
  fi
  if [ "$UPLOAD_FAILURE" == "true" ] || [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "${COMMAND}"
  fi
    
  if [ "$UPLOAD_FAILURE" == "true" ] || [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "${RESPONSE}"
  fi

  new_file_id=$(echo "$RESPONSE" | jq -r '.entries[0] | .id' || echo "error" )
  # If we haven't received the file's id back, it will probably mean that it was not created... so we retry
  if [ "$new_file_id" == "" ] && [ "$RESPONSE" != "" ]; then
    UPLOAD_FAILURE=$(upload_file "$PARENT_ID" "$FILE_PATH")
  fi

  if [ "$UPLOAD_FAILURE" = true ]; then
    echo "################################################"
    echo "############   UPLOAD FAILED   #################"
    echo "################################################"
    echo "FAILURE"
  else
    echo "SUCCESS"
  fi
}


if [[ -z "${FOLDER_ID_MAP[$PARENT_FOLDER_NAME]}" ]]; then
  echo "Box folder $PARENT_FOLDER_NAME is unknown to the FOLDER_ID_MAP associative array in this script.  Please request it to be added."
  exit 1
fi

FOLDER_ID=${FOLDER_ID_MAP[$PARENT_FOLDER_NAME]}

get_access_token

# get $VERSION dir under parent
TARGET_FOLDER_PATH=$PARENT_FOLDER_NAME/$VERSION
get_folder_id "$FOLDER_ID" "$TARGET_FOLDER_PATH"

if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
  echo "FOLDER_ID: $FOLDER_ID"
fi

if [[ "$BUILD_NUM" != "RELEASE" ]]; then
  # create/check $BUILD_NUM dir under $VERSION dir
  TARGET_FOLDER_PATH=$PARENT_FOLDER_NAME/$VERSION/$BUILD_NUM
  get_folder_id "$FOLDER_ID" "$TARGET_FOLDER_PATH"
  if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "FOLDER_ID: $FOLDER_ID"
  fi
fi

TARGET_FOLDER_BASE_PATH=$TARGET_FOLDER_PATH
if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
  echo "TARGET_FOLDER_BASE_PATH: $TARGET_FOLDER_PATH"
fi

ESCAPED_SRC_PATH=$(echo "$SRC_PATH" | sed "s/\//\\\\\//g")

if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
  find $SRC_PATH -type d
fi

SCRIPT_STATUS="SUCCESS"

while IFS= read -r SRC_DIR; do

  TARGET_FOLDER_PATH=$TARGET_FOLDER_BASE_PATH$(echo $SRC_DIR | sed "s/$ESCAPED_SRC_PATH//")
  echo "sending $SRC_DIR to $TARGET_FOLDER_PATH ..."

  if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "FOLDER_ID_MAP:"
    for path_key in "${!FOLDER_ID_MAP[@]}"; do
      echo "   key : ${path_key}" -- "value: ${FOLDER_ID_MAP[${path_key}]}"
    done
  fi

  if [[ -z "${FOLDER_ID_MAP[$TARGET_FOLDER_PATH]}" ]]; then
    if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
      echo "$TARGET_FOLDER_PATH not found in id map"
    fi
    # we need to walk the path and figure out where we need to start creating dirs from
    TARGET_FOLDER_PATH_ARRAY=($(echo $TARGET_FOLDER_PATH | sed "s/\// /g"))
    TARGET_FOLDER_PATH_ARRAY_LENGTH=${#TARGET_FOLDER_PATH_ARRAY[*]}
    if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
      echo "TARGET_FOLDER_PATH_ARRAY: ${TARGET_FOLDER_PATH_ARRAY[*]}"
      echo "TARGET_FOLDER_PATH_ARRAY_LENGTH: $TARGET_FOLDER_PATH_ARRAY_LENGTH"
    fi
    i=0
    while (( $i < ${TARGET_FOLDER_PATH_ARRAY_LENGTH} )); do
      # reconstruct our path segment to check
      for ((j=0; j <= ${i}; j++)); do
        if [[ $j -eq 0 ]]; then
          FOLDER_PATH_SEGMENT=/${TARGET_FOLDER_PATH_ARRAY[$j]}
        else
          FOLDER_PATH_SEGMENT=${FOLDER_PATH_SEGMENT}/${TARGET_FOLDER_PATH_ARRAY[$j]}
        fi
      done
      if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
        echo "checking map for ${FOLDER_PATH_SEGMENT} ..."
      fi
      if [[ -z "${FOLDER_ID_MAP[$FOLDER_PATH_SEGMENT]}" ]]; then
        if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
          echo "$FOLDER_PATH_SEGMENT not found in id map"
        fi
        #start creating dirs from here
        for ((k=${i}; k < ${TARGET_FOLDER_PATH_ARRAY_LENGTH}; k++)); do
          for ((m=0; m <= ${i}; m++)); do
            if [[ $m -eq 0 ]]; then
              FOLDER_PATH_SEGMENT=/${TARGET_FOLDER_PATH_ARRAY[$m]}
            else
              FOLDER_PATH_SEGMENT=${FOLDER_PATH_SEGMENT}/${TARGET_FOLDER_PATH_ARRAY[$m]}
            fi
          done
          echo "creating folder ${FOLDER_PATH_SEGMENT} ..."
          get_folder_id "$FOLDER_ID" "$FOLDER_PATH_SEGMENT"
        done
        break
      else
        FOLDER_ID=${FOLDER_ID_MAP[$FOLDER_PATH_SEGMENT]}
        if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
          echo "FOLDER_ID ${FOLDER_ID} for ${FOLDER_PATH_SEGMENT} found in map"
        fi
      fi
      i=$((i+1))
    done
  else
    if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
      echo "$TARGET_FOLDER_PATH found in id map"
    fi
    FOLDER_ID=${FOLDER_ID_MAP[$TARGET_FOLDER_PATH]}
  fi

  if [ "${IS_DEBUG_ENABLED}" == "true" ]; then
    echo "FOLDER_ID of $TARGET_FOLDER_PATH: $FOLDER_ID"
  fi

  echo "uploading files from $SRC_DIR to $TARGET_FOLDER_PATH ..."

  # get a new access token as they are only good for ~70 mins and these file uploads can take a while
  get_access_token

  find $SRC_DIR -maxdepth 1 -type f | while read real_file; do
    echo "uploading $real_file to $TARGET_FOLDER_PATH ... "
    RESULT=$(upload_file "$FOLDER_ID" "$real_file")
    if [ "$RESULT" == "FAILURE" ]; then
      SCRIPT_STATUS="FAILURE"
    fi
  done

  find $SRC_DIR -maxdepth 1 -type l | while read sym_file; do
    echo "uploading $sym_file to $TARGET_FOLDER_PATH ... "
    RESULT=$(upload_file "$FOLDER_ID" "$sym_file")
    if [ "$RESULT" == "FAILURE" ]; then
      SCRIPT_STATUS="FAILURE"
    fi
  done

done <<< "$(find $SRC_PATH -type d)"

if [ "$SCRIPT_STATUS" == "FAILURE" ]; then
  echo "FAILURES encountered during upload"
  exit 1
fi