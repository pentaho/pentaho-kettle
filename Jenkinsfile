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
    string(defaultValue: '', description: 'The first job in the yaml file to build (empty = first yaml item)',
      name: 'FIRST_JOB')
    string(defaultValue: '', description: 'The last job in the yaml file to build (empty = last yaml item)',
      name: 'LAST_JOB')

    string(defaultValue: '.*-SNAPSHOT.*', description: 'Clean build dependency caches with regex', name: 'CLEAN_CACHES_REGEX')

    string(defaultValue: '-B -e -q', description: 'Force base maven command options',
      name: 'MAVEN_DEFAULT_COMMAND_OPTIONS')
    string(defaultValue: 'clean-all resolve dist', description: 'Force base ant command options',
          name: 'ANT_DEFAULT_COMMAND_OPTIONS')
    string(defaultValue: 'test jacoco', description: 'Ant test targets', name: 'ANT_TEST_TARGETS')

    string(defaultValue: '-Xms512m', description: 'Typically the JVM opts for maven', name: 'MAVEN_OPTS')
    string(defaultValue: '-DsurefireArgLine=-Xmx1g', description: 'Typically, some extra for Maven Surefire',
      name: 'MAVEN_TEST_OPTS')
    string(defaultValue: '10', description: 'Maximum parallel source checkout chunk size',
      name: 'PARALLEL_CHECKOUT_CHUNKSIZE')
    string(defaultValue: '20', description: 'Shallow clone depth (leave blank for infinite)', name: 'CHECKOUT_DEPTH')
    string(defaultValue: '360', description: 'Build timeout in minutes', name: 'BUILD_TIMEOUT')
    string(defaultValue: '1', description: 'Build retry count', name: 'BUILD_RETRIES')
    string(defaultValue: 'http://nexus.pentaho.org/content/groups/omni',
      description: 'Maven resolve repo global mirror', name: 'MAVEN_RESOLVE_REPO_URL')
    string(defaultValue: 'github-buildguy', description: 'Use this Jenkins credential for checkouts',
      name: 'CHECKOUT_CREDENTIALS_ID')
    string(defaultValue: 'Java8_auto', description: 'Use this Jenkins JDK label for builds',
      name: 'JENKINS_JDK_FOR_BUILDS')

    string(defaultValue: 'maven3-auto', description: 'Use this Jenkins Maven label for builds',
      name: 'JENKINS_MAVEN_FOR_BUILDS')
    string(defaultValue: 'ant-auto', description: 'Use this Jenkins Ant label for builds',
          name: 'JENKINS_ANT_FOR_BUILDS')


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

  triggers {
    // Would like conditional polling not available yet.
    // https://issues.jenkins-ci.org/browse/JENKINS-42643
    // You should enable polling for multi-branch until/unless call back hook triggering is done
    pollSCM("")
  }

  environment {
    RESOLVE_REPO_MIRROR = "${params.MAVEN_RESOLVE_REPO_URL}"
    LIB_CACHE_ROOT_PATH = "${WORKSPACE}/caches"
    BUILDS_ROOT_PATH = "${WORKSPACE}/builds"
    MAVEN_HOME = tool('maven3-auto')
    PATH = "$MAVEN_HOME/bin:$PATH"
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
        junit allowEmptyResults: true, testResults: '**/bin/**/TEST*.xml **/target/**/TEST*.xml'
      }
    }

    stage('Archive Artifacts') {
      when {
        expression {
          return (params.ARCHIVE_ARTIFACTS && !params.NOOP)
        }
      }
      steps {
        archiveArtifacts artifacts: '**/dist/*.jar, **/dist/*.gz, **/dist/*.tar.gz, **/dist/*.zip, **/target/*.jar, **/target/*.gz, **/target/*.tar.gz, **/target/*.zip', fingerprint: false
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
