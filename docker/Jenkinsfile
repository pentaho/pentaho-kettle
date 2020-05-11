pipeline {
  agent any
  environment {
    version      = 'nightly'
  }
  stages {
    stage('Build') {
      steps {
        sh '''
          docker build --build-arg version=$version --build-arg CACHEBUST=$BUILD_NUMBER -t hiromuhota/webspoon:$version --pull=true .
        '''
      }
    }
    stage('Publish') {
      steps {
          sh '''
            docker push hiromuhota/webspoon:$version
          '''
      }
    }
  }
}
