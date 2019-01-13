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
                        docker-machine create --driver openstack CEGA_${GIT_COMMIT}
                        eval "$(docker-machine env CEGA_${GIT_COMMIT})"
                        docker swarm init
                      '''
            },
            LEGA: {
                      sh '''
                        docker-machine create --driver openstack LEGA_${GIT_COMMIT}
                        eval "$(docker-machine env LEGA_${GIT_COMMIT})"
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
                        eval "$(docker-machine env CEGA_${GIT_COMMIT})"
                        gradle :cega:createConfiguration
                      '''
            },
            LEGA: {
                      sh '''
                        eval "$(docker-machine env LEGA_${GIT_COMMIT})"
                        gradle :lega-private:createConfiguration
                        gradle :lega-public:createConfiguration
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
                        eval "$(docker-machine env CEGA_${GIT_COMMIT})"
                        gradle :cega:deployStack
                        sleep 120
                        gradle ls
                      '''
            },
            LEGA: {
                      sh '''
                        eval "$(docker-machine env LEGA_${GIT_COMMIT})"
                        gradle :lega-private:deployStack
                        gradle :lega-public:deployStack
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
          gradle ingest -PcegaIP=${docker-machine ip CEGA_${GIT_COMMIT}} -PlegaIP=${docker-machine ip LEGA_${GIT_COMMIT}}
        '''
      }
    }
  }
  
  post('Remove VM') { 
    always {
        sh '''
          eval "$(docker-machine env CEGA_${GIT_COMMIT})"
          echo '---=== cega_cega-mq Logs ===---'
          docker service logs cega_cega-mq
          eval "$(docker-machine env LEGA_${GIT_COMMIT})"
          echo '---=== lega-public_inbox Logs ===---'
          docker service logs lega-public_inbox
          echo '---=== lega-public_mq Logs ===---'
          docker service logs lega-public_mq
          echo '---=== lega-private_private-mq Logs ===---'
          docker service logs lega-private_private-mq
          echo '---=== lega-private_ingest Logs ===---'
          docker service logs lega-private_ingest
          echo '---=== lega-private_s3 Logs ===---'
          docker service logs lega-private_s3
          echo '---=== lega-private_db Logs ===---'
          docker service logs lega-private_db
          echo '---=== lega-private_verify Logs ===---'
          docker service logs lega-private_verify
        '''
      }
    cleanup { 
      sh 'docker-machine rm -y CEGA_${GIT_COMMIT} LEGA_${GIT_COMMIT}'
    }
  }
  
}
