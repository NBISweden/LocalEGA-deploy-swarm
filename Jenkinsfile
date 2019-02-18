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
    GIT_COMMIT_SHORT = sh(
                    script: "printf \$(git rev-parse --short ${GIT_COMMIT})${BUILD_NUMBER}",
                    returnStdout: true
            )
  }
  
  stages {
    stage('Create VMs') {
      steps {
      parallel(
            "CEGA": {
                      sh '''
                        gradle --stacktrace -i :cluster:createCEGAMachine -Pmachine=CEGA-${GIT_COMMIT_SHORT}
                      '''
            },
            "LEGA Public": {
                      sh '''
                        gradle --stacktrace -i :cluster:createLEGAPublicMachine -Pmachine=LEGA-public-${GIT_COMMIT_SHORT}
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle --stacktrace -i :cluster:createLEGAPrivateMachine -Pmachine=LEGA-private-${GIT_COMMIT_SHORT}
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
                        gradle --stacktrace -i :cega:createConfiguration -Pmachine=CEGA-${GIT_COMMIT_SHORT}
                      '''
            },
            "LEGA": {
                      sh '''
                        gradle --stacktrace -i :lega-private:createConfiguration -Pmachine=LEGA-private-${GIT_COMMIT_SHORT}
                        gradle --stacktrace -i :lega-public:createConfiguration -Pmachine=LEGA-public-${GIT_COMMIT_SHORT} -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT_SHORT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT_SHORT})
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
                        gradle --stacktrace -i :cega:deployStack -Pmachine=CEGA-${GIT_COMMIT_SHORT}
                      '''
            },
            "LEGA Public": {
                      sh '''
                        gradle --stacktrace -i :lega-public:deployStack -Pmachine=LEGA-public-${GIT_COMMIT_SHORT}
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle --stacktrace -i :lega-private:deployStack -Pmachine=LEGA-private-${GIT_COMMIT_SHORT}
                      '''
            }
          )
      }
    }
    stage('Initialization') {
      steps {
        sh '''
          sleep 70
        '''
      }
    }
    stage('Test') {
      steps {
        sh '''
          gradle --stacktrace -i ingest -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT_SHORT}) -PlegaPublicIP=$(docker-machine ip LEGA-public-${GIT_COMMIT_SHORT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT_SHORT})
        '''
      }
    }
  }
  
  post('Remove VM') {
    cleanup {
      sh 'docker-machine rm -y CEGA-${GIT_COMMIT_SHORT} LEGA-public-${GIT_COMMIT_SHORT} LEGA-private-${GIT_COMMIT_SHORT}'
    }
  }
  
}
