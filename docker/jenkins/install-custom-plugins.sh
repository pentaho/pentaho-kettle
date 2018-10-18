#!/usr/bin/env bash

set -o pipefail

REF_DIR=${REF:-/usr/share/jenkins/ref/plugins}

FOLDER_PROPERTIES_SHA=818d6c6e9284f4527cbaea6d3ed2b67eabb9be8b5ca0496aea9f1747e44f411b
FOLDER_PROPERTIES_URL=https://public.nexus.pentaho.org/content/groups/omni/com/mig82/folder-properties/1.0/folder-properties-1.0.hpi

WORKFLOW_SCM_STEP_SHA=b9b0d5c4d8eb1a680917475dcd9551a52961c4954d4448a690abbe1755130019
WORKFLOW_SCM_STEP_URL=https://public.nexus.pentaho.org/content/groups/omni/org/jenkins-ci/plugins/workflow/workflow-scm-step/2.7-pentaho/workflow-scm-step-2.7-pentaho.hpi

PIPELINE_CI_UTILITY_STEPS_SHA=f3fb88fc6a3111a71daf03b0c383f498de46e8aedb6a91a8759ffa3a57b08b1d
PIPELINE_CI_UTILITY_STEPS_URL=http://private.nexus.pentaho.org/content/repositories/private-release/org/hitachivantara/ci/plugins/pipeline-ci-utility-steps/1.2/pipeline-ci-utility-steps-1.2.hpi

curl -fsSL -o ${REF_DIR}/folder-properties.jpi ${FOLDER_PROPERTIES_URL}
echo "${FOLDER_PROPERTIES_SHA}  ${REF_DIR}/folder-properties.jpi" | sha256sum -c -

curl -fsSL -o ${REF_DIR}/workflow-scm-step.jpi ${WORKFLOW_SCM_STEP_URL}
echo "${WORKFLOW_SCM_STEP_SHA}  ${REF_DIR}/workflow-scm-step.jpi" | sha256sum -c -

curl -fsSL -o ${REF_DIR}/pipeline-ci-utility-steps.jpi ${PIPELINE_CI_UTILITY_STEPS_URL}
echo "${PIPELINE_CI_UTILITY_STEPS_SHA}  ${REF_DIR}/pipeline-ci-utility-steps.jpi" | sha256sum -c -


ATTEMPTS=-1 install-plugins.sh folder-properties workflow-scm-step pipeline-ci-utility-steps
