@Library('jenkins-shared-libraries') _

// We need a global mapped build data object to pass down through the stages of the build
def mappedBuildData

pipeline {

  agent {
    label 'non-master'
  }

  parameters {

    string(
        name: 'BUILD_DATA_FILE',
        defaultValue: 'buildControlData.yaml',
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
        name: 'MAVEN_DEFAULT_COMMAND_OPTIONS',
        defaultValue: '-B -e -q',
        description: 'Force base maven command options'
    )
    string(
        name: 'ANT_DEFAULT_COMMAND_OPTIONS',
        defaultValue: 'clean-all resolve dist',
        description: 'Force base ant command options'
    )
    string(
        name: 'ANT_TEST_TARGETS',
        defaultValue: 'test jacoco',
        description: 'Ant test targets'
    )
    string(
        name: 'MAVEN_OPTS',
        defaultValue: '-Xms512m',
        description: 'Typically the JVM opts for maven'
    )
    string(
        name: 'MAVEN_TEST_OPTS',
        defaultValue: '-DsurefireArgLine=-Xmx1g',
        description: 'Typically, some extra for Maven Surefire'
    )
    string(
        name: 'PARALLEL_CHECKOUT_CHUNKSIZE',
        defaultValue: '10',
        description: 'Maximum parallel source checkout chunk size'
    )
    string(
        name: 'PARALLEL_UNIT_TESTS_CHUNKSIZE', 
        defaultValue: '10', 
        description: 'Maximum parallel unit tests chunk size'
    )
    string(
        name: 'CHECKOUT_DEPTH',
        defaultValue: '20',
        description: 'Shallow clone depth (leave blank for infinite)'
    )
    string(
        name: 'BUILD_TIMEOUT',
        defaultValue: '360',
        description: 'Build timeout in minutes'
    )
    string(
        name: 'BUILD_RETRIES',
        defaultValue: '1',
        description: 'Build retry count'
    )
    string(
        name: 'MAVEN_RESOLVE_REPO_URL',
        defaultValue: 'http://nexus.pentaho.org/content/groups/omni',
        description: 'Maven resolve repo global mirror'
    )
    string(
        name: 'CHECKOUT_CREDENTIALS_ID',
        defaultValue: 'github-buildguy',
        description: 'Use this Jenkins credential for checkouts'
    )
    string(
        name: 'JENKINS_JDK_FOR_BUILDS',
        defaultValue: 'Java8_auto',
        description: 'Use this Jenkins JDK label for builds'
    )
    string(
        name: 'JENKINS_MAVEN_FOR_BUILDS',
        defaultValue: 'maven3-auto',
        description: 'Use this Jenkins Maven label for builds',
    )
    string(
        name: 'JENKINS_ANT_FOR_BUILDS',
        defaultValue: 'ant-auto',
        description: 'Use this Jenkins Ant label for builds',
    )
    string(
        name: 'CHECKOUT_TIMESTAMP',
        defaultValue: 'CURRENT_TIME',
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
    booleanParam(
       name: 'PUSH_CHANGES',
        defaultValue: false,
        description: 'Push changes in the projects back to the remote origin of the project.<br>'+
      'We would typically use this to update versions and push them to a branch<br>This parameter will get passed to all jobs,'+
      ' and from them to the managed scripts that handle the git push.  (It will override any tagging options set for this job.)',
    )
    booleanParam(
        name: 'USE_DISTRIBUTED_SOURCE_CACHING',
        defaultValue: false,
        description: 'Distributes source checkouts on remote nodes ' +
            '(Otherwise assume workspace is shared on all). Not yet fully implemented--do not use.'
    )

  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  triggers {
    // Would like conditional polling not available yet.
    // https://issues.jenkins-ci.org/browse/JENKINS-42643
    pollSCM("H/60 * * * *")
  }

  environment {
    DEFAULT_BUILD_PROPERTIES = "${WORKSPACE}/resources/config/buildProperties.yaml"
    BUILD_DATA_ROOT_PATH = "${WORKSPACE}/resources/builders"
    RESOLVE_REPO_MIRROR = "${params.MAVEN_RESOLVE_REPO_URL}"
    LIB_CACHE_ROOT_PATH = "${WORKSPACE}/caches"
    BUILD_ROOT_PATH = "${WORKSPACE}/builds"
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
          mappedBuildData = doConfig()
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

    stage('Version') {
      steps {
        doVersioning(mappedBuildData)
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

    stage('Push Changes') {
      when {
        expression {
          return (params.PUSH_CHANGES && !params.NOOP)
        }
      }
      failFast true
      steps {
          doPushChanges(mappedBuildData)
      }
    }

    stage('Tag') {
      when {
        expression {
          return (!params.PUSH_CHANGES && !params.NOOP)
        }
      }
      failFast true
      steps {
          doTag(mappedBuildData)
      }
    }

    stage('Archive Artifacts') {
      when {
        expression {
          return (params.ARCHIVE_ARTIFACTS && !params.NOOP)
        }
      }
      steps {
        archiveArtifacts artifacts: '**/dist/*.jar, **/dist/*.gz, **/dist/*.tar.gz, **/dist/*.zip, **/target/*.jar, **/target/*.gz, **/target/*.tar.gz, **/target/*.zip', excludes: '**/dist/*-sources.jar, **/target/*-sources.jar', allowEmptyArchive: true, fingerprint: false
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
