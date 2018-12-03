pipeline {
  
  agent any
  
  environment {
    OS_USERNAME=credentials('OS_USERNAME')
    OS_PASSWORD=credentials('OS_PASSWORD')
    OS_TENANT_ID=credentials('OS_TENANT_ID')
    OS_DOMAIN_NAME=credentials('OS_DOMAIN_NAME')
    OS_AUTH_URL=credentials('OS_AUTH_URL')
    OS_IDENTITY_API_VERSION=credentials('OS_IDENTITY_API_VERSION')
    OS_REGION_NAME=credentials('OS_REGION_NAME')
    OS_NETWORK_NAME=credentials('OS_NETWORK_NAME')
    OS_SECURITY_GROUPS=credentials('OS_SECURITY_GROUPS')
    OS_SSH_USER=credentials('OS_SSH_USER')
    OS_FLAVOR_NAME=credentials('OS_FLAVOR_NAME')
    OS_IMAGE_ID=credentials('OS_IMAGE_ID')
  }
  
  stages {
    stage('Create VM') {
      steps {
        sh '''
          docker-machine create --driver openstack ${GIT_COMMIT}
          eval "$(docker-machine env ${GIT_COMMIT})"
          docker swarm init
        '''
      }
    }
    stage('Bootstrap') {
      steps {
        sh '''
          eval "$(docker-machine env ${GIT_COMMIT})"
          gradle bootstrap
        '''
      }
    }
    stage('Deploy') {
      steps {
        sh '''
          eval "$(docker-machine env ${GIT_COMMIT})"
          gradle deploy
          sleep 360
          gradle ls
        '''
      }
    }
    stage('Test') {
      steps {
        sh '''
          eval "$(docker-machine env ${GIT_COMMIT})"
          gradle ingest
        '''
      }
    }
  }
  
  post('Remove VM') { 
    cleanup { 
      sh 'docker-machine rm -y ${GIT_COMMIT}'
    }
  }
  
}
