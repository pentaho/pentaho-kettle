@Library('jenkins-shared-libraries') _

// We need a global mapped build data object to pass down through the stages of the build
def mappedBuildData


pipeline {

  agent {
    label 'non-master'
  }

  parameters {

    string(defaultValue: 'buildControlData.yaml', description: 'The build data yaml file to run',
      name: 'BUILD_DATA_FILE')
    string(defaultValue: '.*-SNAPSHOT.*', description: 'Clean build dependency caches with regex', name: 'CLEAN_CACHES_REGEX')
    string(defaultValue: 'http://nexus.pentaho.org/content/groups/omni',
      description: 'Maven resolve repo global mirror', name: 'MAVEN_RESOLVE_REPO_URL')
    string(defaultValue: '60', description: 'Build timeout in minutes', name: 'BUILD_TIMEOUT')

    booleanParam(defaultValue: true, description: 'Run the scm checkouts', name: 'RUN_CHECKOUTS')
    booleanParam(defaultValue: true, description: 'Run the code builds', name: 'RUN_BUILDS')
    booleanParam(defaultValue: true, description: 'Run the code tests', name: 'RUN_UNIT_TESTS')
    booleanParam(defaultValue: true, description: 'Archive the artifacts', name: 'ARCHIVE_ARTIFACTS')


    booleanParam(defaultValue: false, description: 'Clean all build dependency caches', name: 'CLEAN_ALL_CACHES')
    booleanParam(defaultValue: false, description: 'Clean build scm workspaces', name: 'CLEAN_SCM_WORKSPACES')
    booleanParam(defaultValue: false, description: 'Clean build workspace (this happens post build)', name: 'CLEAN_BUILD_WORKSPACE')

    booleanParam(defaultValue: false, description: 'No op build (test the build config)', name: 'NOOP')
    booleanParam(defaultValue: false, description: 'Distributes source checkouts on remote nodes ' +
      '(Otherwise assume workspace is shared on all). Not yet fully implemented--do not use.', name: 'USE_DISTRIBUTED_SOURCE_CACHING')

  }

  options {
    timestamps()
  }

  environment {
    RESOLVE_REPO_MIRROR = "${params.MAVEN_RESOLVE_REPO_URL}"
    LIB_CACHE_ROOT_PATH = "${WORKSPACE}/caches"
    BUILDS_ROOT_PATH = "${WORKSPACE}/builds"
  }


  stages {

    stage('Clean Regex Lib Caches') {
      when {
        expression {
          return (params.CLEAN_CACHES_REGEX && !params.CLEAN_ALL_CACHES)
        }
      }
      steps {
        dir("${LIB_CACHE_ROOT_PATH}") {
          doRegexCacheClean()
        }
      }
    }

    stage('Clean All Lib Caches') {
      when {
        expression {
          return params.CLEAN_ALL_CACHES
        }
      }
      steps {
        dir("${LIB_CACHE_ROOT_PATH}") {
          deleteDir()
        }
      }
    }

    stage('Clean Workspaces') {
      when {
        expression {
          return params.CLEAN_SCM_WORKSPACES
        }
      }
      steps {
        dir("${BUILDS_ROOT_PATH}") {
          deleteDir()
        }
      }
    }


    stage('Configure Pipeline') {
      steps {
        script {
          mappedBuildData = doConfig("${WORKSPACE}/resources/builders/${params.BUILD_DATA_FILE}")
        }
      }
    }

    stage('Checkouts') {
      when {
        expression {
          return params.RUN_CHECKOUTS
        }
      }
      steps {
        doCheckouts(mappedBuildData)
      }
    }

    stage('Build') {
      when {
        expression {
          return params.RUN_BUILDS
        }
      }
      failFast true
      steps {
        timeout(time: Integer.valueOf(params.BUILD_TIMEOUT), unit: 'MINUTES') {
          doBuilds(mappedBuildData)
        }
      }
    }

    stage('Unit Test') {
      when {
        expression {
          return params.RUN_UNIT_TESTS
        }
      }
      failFast true
      steps {
        timeout(time: Integer.valueOf(params.BUILD_TIMEOUT), unit: 'MINUTES') {
          doUnitTests(mappedBuildData)
        }
      }
    }

    stage('Archive Test Results') {
      when {
        expression {
          return (params.RUN_UNIT_TESTS && !params.NOOP)
        }
      }
      steps {
        junit allowEmptyResults: true, testResults: '**/target/**/TEST*.xml'
      }
    }

    stage('Archive Artifacts') {
      when {
        expression {
          return (params.ARCHIVE_ARTIFACTS && !params.NOOP)
        }
      }
      steps {
        archiveArtifacts artifacts: '**/target/*.gz, **/target/*.tar.gz, **/target/*.zip', fingerprint: false
      }
    }

    stage('Clean Workspace') {
      when {
        expression {
          return params.CLEAN_BUILD_WORKSPACE
        }
      }
      steps {
        deleteDir() /* clean up our workspace */
      }
    }
  }

  post {
    always {
      echo 'One way or another, I have finished'
    }
    success {
      //slackSend channel: "buildteam-alerts", color: 'good', message: "Build Succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
      echo 'I succeeeded!'
    }
    unstable {
      //slackSend channel: "buildteam-alerts", color: 'warning', message: "Build Unstable: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
      echo 'I am unstable :/'
    }
    failure {
      //slackSend channel: "buildteam-alerts", color: 'danger', message: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
      echo 'I failed :('
    }
    changed {
      echo 'Things were different before...'
    }
  }
}
