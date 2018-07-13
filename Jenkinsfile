@Library('jenkins-shared-libraries') _

// Global BuildData object containing all the configuration
// needed to pass down through the stages of the build
def buildData
Map buildProperties

pipeline {

  agent {
    label 'non-master'
  }

  parameters {

    string(
        name: 'BUILD_DATA_FILE',
        defaultValue: jobNameBuildFile(),
        description: 'The build data yaml file to run'
    )
    string(
        name: 'FIRST_JOB',
        defaultValue: '',
        description: 'The first job in the yaml file to build (empty = first yaml item)'
    )
    string(
        name: 'LAST_JOB',
        defaultValue: '',
        description: 'The last job in the yaml file to build (empty = last yaml item)'
    )
    string(
        name: 'RELEASE_BUILD_NUMBER',
        defaultValue: '',
        description: 'Override the build number. Leave this blank to use the actual build number',
    )
    string(
        name: 'CLEAN_CACHES_REGEX',
        defaultValue: '.*-SNAPSHOT.*',
        description: 'Clean build dependency caches with regex'
    )
    string(
        name: 'CHECKOUT_TIMESTAMP',
        defaultValue: '',
        description: 'Determines the timestamp to use as the limit for version control commits. Any commits after the timestamp will not be used. Override format: YYYY-MM-DD HH:MM:SS',
    )

    text(
        name: 'OVERRIDE_JOB_PARAMS',
        defaultValue: '',
        description: 'Override job parameters using the same yaml format as the builders. ' +
            'Example: "{jobID: job1, param1: value1, param2: value2}".'
    )

    booleanParam(
        name: 'RUN_CHECKOUTS',
        defaultValue: true,
        description: 'Run the scm checkouts'
    )
    booleanParam(
        name: 'RUN_BUILDS',
        defaultValue: true,
        description: 'Run the code builds'
    )
    booleanParam(
        name: 'RUN_UNIT_TESTS',
        defaultValue: true,
        description: 'Run the code tests'
    )
    booleanParam(
        name: 'ARCHIVE_ARTIFACTS',
        defaultValue: true,
        description: 'Archive the artifacts'
    )
    booleanParam(
        name: 'CLEAN_ALL_CACHES',
        defaultValue: false,
        description: 'Clean all build dependency caches'
    )
    booleanParam(
        name: 'CLEAN_SCM_WORKSPACES',
        defaultValue: false,
        description: 'Clean build scm workspaces'
    )
    booleanParam(
        name: 'CLEAN_BUILD_WORKSPACE',
        defaultValue: false,
        description: 'Clean build workspace (this happens post build)'
    )
    booleanParam(
        name: 'NOOP',
        defaultValue: false,
        description: 'No op build (test the build config)'
    )

  }

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '45', artifactNumToKeepStr: '3'))
  }

  environment {
      DEFAULT_BUILD_PROPERTIES = "${WORKSPACE}/resources/config/buildProperties.yaml"
      BUILD_DATA_ROOT_PATH = "${WORKSPACE}/resources/builders"
      RESOLVE_REPO_MIRROR = "${params.MAVEN_RESOLVE_REPO_URL}"
      LIB_CACHE_ROOT_PATH = "${WORKSPACE}/caches"
      BUILDS_ROOT_PATH = "${WORKSPACE}/builds"
  }

  triggers {
    // Would like conditional polling not available yet.
    // https://issues.jenkins-ci.org/browse/JENKINS-42643
    pollSCM(doPollScm())
  }

  stages {
    stage('Configure Pipeline') {
      // After this stage all properties will be available in the buildProperties Map
      steps {
        script {
          buildData = doConfig()
          buildProperties = buildData.buildProperties
        }
      }
    }

    stage('Clean Caches') {
      when {
        expression {
          buildProperties.CLEAN_CACHES_REGEX || buildProperties.CLEAN_ALL_CACHES
        }
      }
      steps {
        doCacheClean(buildData)
      }
    }

    stage('Clean Project Workspaces') {
      when {
        expression {
          buildProperties.CLEAN_SCM_WORKSPACES
        }
      }
      steps {
        dir(buildProperties.BUILDS_ROOT_PATH) {
          deleteDir()
        }
      }
    }

    stage('Checkouts') {
      when {
        expression {
          buildProperties.RUN_CHECKOUTS
        }
      }
      steps {
          doCheckouts(buildData)
      }
    }

    stage('Version') {
      steps {
        doVersioning(buildData)
      }
    }

    stage('Build') {
      when {
        expression {
          buildProperties.RUN_BUILDS
        }
      }
      steps {
        timeout(time: buildData.buildTimeout, unit: 'MINUTES') {
          doBuilds(buildData)
        }
      }
    }

    stage('Unit Test') {
      when {
        expression {
          buildProperties.RUN_UNIT_TESTS
        }
      }
      steps {
        timeout(time: buildData.buildTimeout, unit: 'MINUTES') {
          doUnitTests(buildData)
        }
      }
    }

    stage('Push Changes') {
      when {
        expression {
          buildProperties.PUSH_CHANGES && !buildProperties.NOOP
        }
      }
      steps {
          doPushChanges(buildData)
      }
    }

    stage('Tag') {
      when {
        expression {
          !buildProperties.PUSH_CHANGES && !buildProperties.NOOP && buildProperties.TAG_NAME_TYPE != 'NONE'
        }
      }
      steps {
          doTag(buildData)
      }
    }

    stage('Archive Artifacts') {
      when {
        expression {
          buildProperties.ARCHIVE_ARTIFACTS && !buildProperties.NOOP
        }
      }
      steps {
        doArtifactArchiving(buildData)
      }
    }

    stage('Clean Workspace') {
      when {
        expression {
          buildProperties.CLEAN_BUILD_WORKSPACE
        }
      }
      steps {
        deleteDir() /* clean up our workspace */
      }
    }
  }

  post {
    always {
      doReport(buildData)
    }
  }
}
