pipeline {
  
  agent any
  
  options { disableConcurrentBuilds() }
  
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
                        gradle --stacktrace -i :cluster:createCEGAMachine -Pmachine=CEGA-${GIT_COMMIT_SHORT} --stacktrace
                      '''
            },
            "LEGA Public": {
                      sh '''
                        gradle --stacktrace -i :cluster:createLEGAPublicMachine -Pmachine=LEGA-public-${GIT_COMMIT_SHORT} --stacktrace
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle --stacktrace -i :cluster:createLEGAPrivateMachine -Pmachine=LEGA-private-${GIT_COMMIT_SHORT} --stacktrace
                      '''
            }
          )
      }
    }
    stage('Bootstrap') {
      steps {
          sh '''
            gradle --stacktrace -i :cega:createConfiguration -Pmachine=CEGA-${GIT_COMMIT_SHORT} --stacktrace
            gradle --stacktrace -i :lega-private:createConfiguration -Pmachine=LEGA-private-${GIT_COMMIT_SHORT} --stacktrace
            gradle --stacktrace -i :lega-public:createConfiguration -Pmachine=LEGA-public-${GIT_COMMIT_SHORT} -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT_SHORT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT_SHORT}) --stacktrace
          '''
      }
    }
    stage('Deploy') {
      steps {
      parallel(
            "CEGA": {
                      sh '''
                        gradle --stacktrace -i :cega:deployStack -Pmachine=CEGA-${GIT_COMMIT_SHORT} --stacktrace
                      '''
            },
            "LEGA Public": {
                      sh '''
                        gradle --stacktrace -i :lega-public:deployStack -Pmachine=LEGA-public-${GIT_COMMIT_SHORT} --stacktrace
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle --stacktrace -i :lega-private:deployStack -Pmachine=LEGA-private-${GIT_COMMIT_SHORT} --stacktrace
                      '''
            }
          )
      }
    }
    stage('Initialization') {
      steps {
        sh '''
          sleep 180
        '''
      }
    }
    stage('Test') {
      steps {
        sh '''
          gradle --stacktrace -i ingest -PcegaIP=$(docker-machine ip CEGA-${GIT_COMMIT_SHORT}) -PlegaPublicIP=$(docker-machine ip LEGA-public-${GIT_COMMIT_SHORT}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${GIT_COMMIT_SHORT}) --stacktrace
        '''
      }
    }
    
    stage('master'){
      environment {
        LEGA_private_IP = sh(
                        script: "printf \$(docker-machine ip lega-private-staging)",
                        returnStdout: true
                )
        LEGA_public_IP = sh(
                        script: "printf \$(docker-machine ip lega-public-staging)",
                        returnStdout: true
                )
        CEGA_IP = sh(
                        script: "printf \$(docker-machine ip cega-staging)",
                        returnStdout: true
                )
        }
        when {
           branch "feature/isolate-networks"
        }
        stages{
          stage('Tear down') {
            steps {
                sh '''
                  gradle :cega:removeStack -Pmachine=cega-staging --stacktrace
                  gradle :lega-private:removeStack -Pmachine=lega-private-staging --stacktrace
                  gradle :lega-public:removeStack -Pmachine=lega-public-staging --stacktrace
    
                  sleep 10
    
                  gradle prune -Pmachine=cega-staging --stacktrace
                  gradle prune -Pmachine=lega-private-staging --stacktrace
                  gradle prune -Pmachine=lega-public-staging --stacktrace
                '''
            }
          }
         
          stage('Bootstrap') {
            steps {
                sh '''
                  gradle :cega:createConfiguration \
                      -Pmachine=cega-staging \
                      --stacktrace
      
                  gradle :lega-private:createConfiguration \
                      -Pmachine=lega-private-staging \
                      --stacktrace
      
                  gradle :lega-public:createConfiguration \
                      -Pmachine=lega-public-staging \
                      -PcegaIP=${CEGA_IP} \
                      -PlegaPrivateIP=${LEGA_private_IP} \
                      --stacktrace
                '''
            }
          }
      
          stage('Deploy') {
            steps {
            parallel(
                  "CEGA": {
                    sh '''
                      gradle :cega:deployStack -Pmachine=cega-staging --stacktrace
                    '''
                  },
                  "LEGA Public": {
                    sh '''
                      gradle :lega-public:deployStack -Pmachine=lega-public-staging --stacktrace
                    '''
                  },
                  "LEGA Private": {
                    sh '''
                      gradle :lega-private:deployStack -Pmachine=lega-private-staging --stacktrace
                    '''
                  }
                )
            }
          }
          stage('Initialization') {
            steps {
              sh '''
                sleep 80
              '''
            }
          }
          stage('Test') {
            steps {
              sh '''
                gradle ingest \
                  -PcegaIP=${CEGA_IP} \
                  -PlegaPublicIP=${LEGA_public_IP} \
                  -PlegaPrivateIP=${LEGA_private_IP} \
                  --stacktrace
              '''
            }
          }
        }
        
        post('Logging') {
          failure {
              sh '''
                eval "$(docker-machine env lega-public-staging)"
                echo '---=== lega-public-staging_inbox Logs ===---'
                docker service logs lega-public-staging_inbox
                echo '---=== lega-public-staging_mq Logs ===---'
                docker service logs lega-public-staging_mq
              '''
              sh '''
                eval "$(docker-machine env cega-staging)"
                echo '---=== cega-staging_cega-mq Logs ===---'
                docker service logs cega-staging_cega-mq
              '''
              sh '''
                eval "$(docker-machine env lega-private-staging)"
                echo '---=== lega-private-staging-mq Logs ===---'
                docker service logs lega-private-staging_mq
                echo '---=== lega-private-staging_ingest Logs ===---'
                docker service logs lega-private-staging_ingest
                echo '---=== lega-private-staging_inbox-s3 Logs ===---'
                docker service logs lega-private-staging_inbox-s3
                echo '---=== lega-private-staging_vault-s3 Logs ===---'
                docker service logs lega-private-staging_vault-s3
                echo '---=== lega-private-staging_db Logs ===---'
                docker service logs lega-private-staging_db
                echo '---=== lega-private-staging_verify Logs ===---'
                docker service logs lega-private-staging_verify
              '''
          }
        }
    }
    
  }
  
  post('Remove VM') {
    cleanup {
      sh 'docker-machine rm -y CEGA-${GIT_COMMIT_SHORT} LEGA-public-${GIT_COMMIT_SHORT} LEGA-private-${GIT_COMMIT_SHORT}'
    }
  }
  
}
