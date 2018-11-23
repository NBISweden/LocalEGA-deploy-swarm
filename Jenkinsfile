pipeline {
  agent any
  environment {
    OS_USERNAME=credentials('OS_USERNAME')
    OS_PASSWORD=credentials('OS_PASSWORD')
    OS_TENANT_ID='894cf709b66549bd9f35968bdbff90d3'
    OS_DOMAIN_NAME='dataporten'
    OS_AUTH_URL='https://api.uh-iaas.no:5000/v3'
    OS_IDENTITY_API_VERSION='3'
    OS_REGION_NAME='bgo'
    OS_NETWORK_NAME='dualStack'
    OS_SECURITY_GROUPS='default,SSH,Docker,LocalEGA,Web'
    OS_SSH_USER='ubuntu'
    OS_FLAVOR_NAME='m1.large'
    OS_IMAGE_ID='dd945baa-d1a6-481f-b358-91908bc60930'
  }
  stages {
    stage('Create VM') {
      steps {
        sh 'docker-machine create --driver openstack ${GIT_COMMIT}'
      }
    }
    stage('Bootstrap') {
      steps {
        sh 'eval "$(docker-machine env ${GIT_COMMIT})"'
        sh 'gradle bootstrap'
      }
    }
  }
  post('Remove VM') { 
    cleanup { 
      sh 'docker-machine rm -y ${GIT_COMMIT}'
    }
  }
}
