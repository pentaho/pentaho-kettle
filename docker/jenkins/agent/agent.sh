#!/bin/bash

# if `docker run` first argument start with `-` the user is passing jenkins swarm launcher arguments
if [[ $# -lt 1 ]] || [[ "$1" == "-"* ]]; then

  # jenkins swarm slave
  JAR=`ls -1 /usr/share/jenkins/swarm-client-*.jar | tail -n 1`

  # Do not overwrite the PARAMS variable if the user chose to provide it when launching the service
  if [ -z "$PARAMS" ]; then

    # if -master is not provided and using --link jenkins:jenkins
    if [[ "$@" != *"-master "* ]] && [ ! -z "$JENKINS_PORT_8080_TCP_ADDR" ]; then
      PARAMS="-master http://$JENKINS_PORT_8080_TCP_ADDR:$JENKINS_PORT_8080_TCP_PORT"
    else
      # Check for parameters in environment variables
      if [ ! -z "$AUTO_DISCOVERY_ADDRESS" ]; then
        PARAMS="$PARAMS -autoDiscoveryAddress $AUTO_DISCOVERY_ADDRESS"
      fi
      if [ ! -z "$CANDIDATE_TAG" ]; then
        PARAMS="$PARAMS -candidateTag $CANDIDATE_TAG"
      fi
      if [ "${DELETE_EXISTING_CLIENTS}x" = "truex" ]; then
        PARAMS="$PARAMS -deleteExistingClients"
      fi
      if [ "${DISABLE_CLIENTS_UNIQUE_ID}x" = "truex" ]; then
        PARAMS="$PARAMS -disableClientsUniqueId"
      fi
      if [ "${DISABLE_SSL_VERIFICATION}x" = "truex" ]; then
        PARAMS="$PARAMS -disableSslVerification"
      fi
      if [ ! -z "$EXECUTORS" ]; then
        PARAMS="$PARAMS -executors $EXECUTORS"
      fi
      if [ ! -z "$FSROOT" ]; then
        PARAMS="$PARAMS -fsroot $FSROOT"
      fi
      if [ ! -z "$LABELS" ]; then
        PARAMS="$PARAMS -labels \"$LABELS\""
      fi
      if [ ! -z "$LABELS_FILE" ]; then
        PARAMS="$PARAMS -labelsFile $LABELS_FILE"
      fi
      if [ ! -z "$MASTER" ]; then
        PARAMS="$PARAMS -master $MASTER"
      fi
      if [ ! -z "$MAX_RETRY_INTERVAL" ]; then
        PARAMS="$PARAMS -maxRetryInterval $MAX_RETRY_INTERVAL"
      fi
      if [ ! -z "$MODE" ]; then
        PARAMS="$PARAMS -mode $MODE"
      fi
      if [ ! -z "$NAME" ]; then
        PARAMS="$PARAMS -name $NAME"
      fi
      if [ "${NO_RETRY_AFTER_CONNECTED}x" = "truex" ]; then
        PARAMS="$PARAMS -noRetryAfterConnected"
      fi
      if [ ! -z "$PASSWORD" ]; then
        PARAMS="$PARAMS -passwordEnvVariable PASSWORD"
      fi
      if [ ! -z "$PASSWORD_FILE" ]; then
        PARAMS="$PARAMS -passwordFile $PASSWORD_FILE"
      fi
      if [ ! -z "$RETRY" ]; then
        PARAMS="$PARAMS -retry $RETRY"
      fi
      if [ ! -z "$RETRY_BACKOFF_STRATEGY" ]; then
        PARAMS="$PARAMS -retryBackOffStrategy $RETRY_BACKOFF_STRATEGY"
      fi
      if [ ! -z "$RETRY_INTERVAL" ]; then
        PARAMS="$PARAMS -retryInterval $NAME"
      fi
      if [ "${SHOW_HOSTNAME}x" = "truex" ]; then
        PARAMS="$PARAMS -showHostName"
      fi
      if [ ! -z "$SSL_FINGERPRINTS" ]; then
        PARAMS="$PARAMS -sslFingerprints $SSL_FINGERPRINTS"
      fi
      if [ ! -z "$TOOLS" ]; then
        # Tools should be declared as "-e TOOLS='-t toolName=toolLocation -t tool2Name=tool2Location'"
        PARAMS="$PARAMS $TOOLS"
      fi
      if [ ! -z "$TUNNEL" ]; then
        PARAMS="$PARAMS -tunnel $TUNNEL"
      fi
      if [ ! -z "$USERNAME" ]; then
        PARAMS="$PARAMS -username $USERNAME"
      fi
    fi
  fi
  echo Running java $JAVA_OPTS -jar $JAR -fsroot $HOME $PARAMS "$@"
  exec java $JAVA_OPTS -jar $JAR -fsroot $HOME $PARAMS "$@"
fi

# As argument is not jenkins, assume user want to run his own process, for sample a `bash` shell to explore this image
exec "$@"
