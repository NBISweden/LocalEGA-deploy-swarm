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
                    script: "printf \$(git rev-parse --short ${GIT_COMMIT})",
                    returnStdout: true
            )
    ID = sh(
                    script: "printf \$(git rev-parse --short ${GIT_COMMIT})${BUILD_NUMBER}",
                    returnStdout: true
            )
    LOGZIO_TOKEN=credentials('LOGZIO_TOKEN')
  }

  stages {
    stage('Create VMs') {
      steps {
      parallel(
//            "CEGA": {
//                      sh '''
//                        gradle :cluster:createCEGAMachine -Pmachine=CEGA-${ID} --stacktrace -i
//                      '''
//            },
            "LEGA Public": {
                      sh '''
                        gradle :cluster:createLEGAPublicMachine -Pmachine=LEGA-public-${ID} -PTEST_CEGA=yes --stacktrace -i
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle :cluster:createLEGAPrivateMachine -Pmachine=LEGA-private-${ID} --stacktrace -i
                      '''
            }
          )
      }
    }

    stage('Bootstrap') {
      steps {
          sh '''
#            gradle :cega:createConfiguration \
#                -Pmachine=CEGA-${ID} \
#                --stacktrace

            gradle :lega-private:createConfiguration \
                -Pmachine=LEGA-private-${ID} \
                -PcegaIP=$(docker-machine ip LEGA-public-${ID}) \
                -PTEST_CEGA=yes \
                --stacktrace

            gradle :lega-public:createConfiguration \
                -Pmachine=LEGA-public-${ID} \
                -PcegaIP=$(docker-machine ip CEGA-${ID}) \
                -PlegaPrivateIP=$(docker-machine ip LEGA-private-${ID}) \
                --stacktrace
          '''
      }
    }

    stage('Deploy') {
      steps {
      parallel(
//            "CEGA": {
//                      sh '''
//                        gradle :cega:deployStack -Pmachine=CEGA-${ID} --stacktrace
//                      '''
//            },
            "LEGA Public": {
                      sh '''
                        gradle :lega-public:deployStack -Pmachine=LEGA-public-${ID} -PTEST_CEGA=yes --stacktrace
                      '''
            },
            "LEGA Private": {
                      sh '''
                        gradle :lega-private:deployStack -Pmachine=LEGA-private-${ID} -PTEST_CEGA=yes --stacktrace
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
          gradle ingest \
          -PlegaPublicIP=$(docker-machine ip LEGA-public-${ID}) \
          -PlegaPrivateIP=$(docker-machine ip LEGA-private-${ID}) \
          -PTEST_CEGA=yes --stacktrace -d
#          -PcegaIP=$(docker-machine ip CEGA-${ID}) \
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
           branch "feature/use-test-cega"
        }
        stages{
          stage('Tear down') {
            steps {
                sh '''
#                  gradle :cega:removeStack -Pmachine=cega-staging --stacktrace
                  gradle :lega-private:removeStack -Pmachine=lega-private-staging --stacktrace
                  gradle :lega-public:removeStack -Pmachine=lega-public-staging --stacktrace

                  sleep 10

#                  gradle prune -Pmachine=cega-staging --stacktrace
                  gradle prune -Pmachine=lega-private-staging --stacktrace
                  gradle prune -Pmachine=lega-public-staging --stacktrace
                '''
            }
          }

          stage('Bootstrap') {
            steps {
                sh '''
#                  gradle :cega:createConfiguration \
#                      -Pmachine=cega-staging \
#                      --stacktrace

                  gradle :lega-private:createConfiguration \
                      -Pmachine=lega-private-staging \
                      -PcegaIP=${LEGA_public_IP} -PTEST_CEGA=yes \
                      --stacktrace

                  gradle :lega-public:createConfiguration \
                      -Pmachine=lega-public-staging \
                      -PcegaIP=${CEGA_IP} \
                      -PlegaPrivateIP=${LEGA_private_IP} -PTEST_CEGA=yes \
                      --stacktrace
                '''
            }
          }

          stage('Deploy') {
            steps {
            parallel(
//                  "CEGA": {
//                    sh '''
//                      gradle :cega:deployStack -Pmachine=cega-staging --stacktrace
//                    '''
//                  },
                  "LEGA Public": {
                    sh '''
                      gradle :lega-public:deployStack -Pmachine=lega-public-staging -PTEST_CEGA=yes --stacktrace
                    '''
                  },
                  "LEGA Private": {
                    sh '''
                      gradle :lega-private:deployStack -Pmachine=lega-private-staging -PTEST_CEGA=yes --stacktrace
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
                  -PlegaPublicIP=${LEGA_public_IP} \
                  -PlegaPrivateIP=${LEGA_private_IP} \
                  -PTEST_CEGA=yes \
                  --stacktrace -d
#                  -PcegaIP=${CEGA_IP} \
              '''
            }
          }
        }
    }
  }

  post('Remove VM') {
    cleanup {
//      sh 'docker-machine rm -y CEGA-${ID} LEGA-public-${ID} LEGA-private-${ID}'
//      sh 'gradle :cluster:removeMachine -Pmachine=CEGA-${ID} --stacktrace -i'
      sh 'gradle :cluster:removeMachine -Pmachine=LEGA-public-${ID} --stacktrace -i'
      sh 'gradle :cluster:removeMachine -Pmachine=LEGA-private-${ID} --stacktrace -i'
    }
  }

}
