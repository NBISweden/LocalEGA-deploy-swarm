pipeline {
  agent any
  stages {
    stage('') {
      steps {
        sh 'docker-machine create --driver openstack ${GIT_COMMIT}'
        sleep 10
      }
    }
  }
}