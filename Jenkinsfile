pipeline {
  
  agent any
  
  triggers {
    cron('0 0 * * *')
    upstream(upstreamProjects: 'LocalEGA Build Trigger')
  }
  
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
    stage('Create VMs') {
      steps {
      parallel(
            CEGA: {
                      sh '''
                        docker-machine create --driver openstack CEGA-${GIT_COMMIT}
                        eval "$(docker-machine env CEGA-${GIT_COMMIT})"
                        docker swarm init
                      '''
            },
            LEGA-Public: {
                      sh '''
                        docker-machine create --driver openstack LEGA-public-${GIT_COMMIT}
                        eval "$(docker-machine env LEGA-public-${GIT_COMMIT})"
                        docker swarm init
                      '''
            },
            LEGA-Private: {
                      sh '''
                        docker-machine create --driver openstack LEGA-private-${GIT_COMMIT}
                        eval "$(docker-machine env LEGA-private-${GIT_COMMIT})"
                        docker swarm init
                      '''
            }
          )
      }
    }
    stage('Bootstrap') {
      steps {
      parallel(
            CEGA: {
                      sh '''
                        eval "$(docker-machine env CEGA-${GIT_COMMIT})"
                        gradle :cega:createConfiguration
                      '''
            },
            LEGA: {
                      sh '''
                        eval "$(docker-machine env LEGA-private-${GIT_COMMIT})"
                        gradle :lega-private:createConfiguration
                        eval "$(docker-machine env LEGA-public-${GIT_COMMIT})"
                        gradle :lega-public:createConfiguration -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT})
                      '''
            }
          )
      }
    }
    stage('Deploy') {
      steps {
      parallel(
            CEGA: {
                      sh '''
                        eval "$(docker-machine env CEGA-${GIT_COMMIT})"
                        gradle :cega:deployStack
                        sleep 120
                        gradle ls
                      '''
            },
            LEGA-Public: {
                      sh '''
                        eval "$(docker-machine env LEGA-public-${GIT_COMMIT})"
                        gradle :lega-public:deployStack
                        sleep 120
                        gradle ls
                      '''
            },
            LEGA-Private: {
                      sh '''
                        eval "$(docker-machine env LEGA-private-${GIT_COMMIT})"
                        gradle :lega-private:deployStack
                        sleep 120
                        gradle ls
                      '''
            }
          )
      }
    }
    stage('Test') {
      steps {
        sh '''
          gradle ingest -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT}) -PlegaPublicIP=$(docker-machine ip LEGA-public-${GIT_COMMIT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT})
        '''
      }
    }
  }
  
  post('Remove VM') {
    cleanup {
      sh 'docker-machine rm -y CEGA-${GIT_COMMIT} LEGA-public-${GIT_COMMIT} LEGA-private-${GIT_COMMIT}'
    }
  }
  
}
