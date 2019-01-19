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
            "CEGA": {
                      sh '''
                        gradle :cluster:createCEGAMachine -Pmachine=CEGA-${GIT_COMMIT}
                      '''
            },
            "LEGA Public": {
                      sh '''
                        gradle :cluster:createLEGAPublicMachine -Pmachine=LEGA-public-${GIT_COMMIT}
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle :cluster:createLEGAPrivateMachine -Pmachine=LEGA-private-${GIT_COMMIT}
                      '''
            }
          )
      }
    }
    stage('Bootstrap') {
      steps {
      parallel(
            "CEGA": {
                      sh '''
                        gradle :cega:createConfiguration -Pmachine=CEGA-${GIT_COMMIT}
                      '''
            },
            "LEGA": {
                      sh '''
                        gradle :lega-private:createConfiguration -Pmachine=LEGA-private-${GIT_COMMIT}
                        gradle :lega-public:createConfiguration -Pmachine=LEGA-public-${GIT_COMMIT} -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT})
                      '''
            }
          )
      }
    }
    stage('Deploy') {
      steps {
      parallel(
            "CEGA": {
                      sh '''
                        gradle :cega:deployStack -Pmachine=CEGA-${GIT_COMMIT}
                        sleep 120
                        gradle ls
                      '''
            },
            "LEGA Public": {
                      sh '''
                        gradle :lega-public:deployStack -Pmachine=LEGA-public-${GIT_COMMIT} --stacktrace
                        sleep 120
                        gradle ls
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle :lega-private:deployStack -Pmachine=LEGA-private-${GIT_COMMIT} --stacktrace
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
