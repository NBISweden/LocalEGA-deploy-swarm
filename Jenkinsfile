pipeline {

  agent any

  options { disableConcurrentBuilds() }

  triggers {
    GenericTrigger(
     genericVariables: [
      [key: 'service', value: '$.service'],
      [key: 'image', value: '$.image']
     ],
     token: 'LocalEGA',
     causeString: 'Triggered by service $service with image $image',
     printContributedVariables: true,
     printPostContent: true
    )
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
    ENV = 'test'
    LOGZIO_TOKEN=credentials('LOGZIO_TOKEN')

    PUBLIC_BROKER_SERVICE='uiobmi/localega-broker-public:latest'
    INBOX_SERVICE='nbisweden/ega-mina-inbox:latest'

    PRIVATE_BROKER_SERVICE='uiobmi/localega-broker-private:latest'
    DB_SERVICE='egarchive/lega-db:latest'
    KEYS_SERVICE='cscfi/ega-keyserver'
    VERIFY_SERVICE='egarchive/lega-base:latest'
    RES_SERVICE='cscfi/ega-res:latest'
    FINALIZE_SERVICE='egarchive/lega-base:latest'
    INGEST_SERVICE='egarchive/lega-base:latest'
  }

  stages {

    stage('setup') {
      steps{
        slackSend (color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
      }
    }

    stage('external') {

      when { triggeredBy cause: "GenericCause" }

      stages {

        stage('Set service image') {
            steps {
                script {
                    echo service
                    echo image
                    env.setProperty(service, image)
                }
            }
        }

        stage('Create VMs') {
          steps {
          parallel(
                "CEGA": {
                          sh '''
                            gradle :cluster:createCEGAMachine -Pmachine=CEGA-${ID} --stacktrace -i
                          '''
                },
                "LEGA Public": {
                          sh '''
                            gradle :cluster:createLEGAPublicMachine -Pmachine=LEGA-public-${ID} --stacktrace -i
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
                gradle :common:createConfiguration \
                    -PlegaPrivateIP=$(docker-machine ip LEGA-private-${ID}) \
                    --stacktrace

                gradle :cega:createConfiguration \
                    -Pmachine=CEGA-${ID} \
                    --stacktrace

                gradle :lega-private:createConfiguration \
                    -Pmachine=LEGA-private-${ID} \
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
                "CEGA": {
                          sh '''
                            gradle :cega:deployStack -Pmachine=CEGA-${ID} --stacktrace
                          '''
                },
                "LEGA Public": {
                          sh '''
                            gradle :lega-public:deployStack -Pmachine=LEGA-public-${ID} --stacktrace
                          '''
                },
                "LEGA Private": {
                          sh '''
                            gradle :lega-private:deployStack -Pmachine=LEGA-private-${ID} --stacktrace
                          '''
                }
              )
          }
        }

        stage('Initialization') {
          steps {
            sh '''
              sleep 120
            '''
          }
        }

        stage('Test') {
          steps {
            sh '''
              gradle verify -PcegaIP=$(docker-machine ip CEGA-${ID}) -PlegaPublicIP=$(docker-machine ip LEGA-public-${ID}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${ID}) --stacktrace
            '''
          }
        }
      }
      post('Remove VM') {
        failure{
          sh '''
            gradle :cluster:serviceLogs -Pmachine=LEGA-public-${ID} -Pservice=inbox --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-public-${ID} -Pservice=mq --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=mq --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=ingest --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=db --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=vault-s3 --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=verify --stacktrace -i
          '''
        }
        cleanup {
          sh 'docker-machine rm -y CEGA-${ID} LEGA-public-${ID} LEGA-private-${ID}'
        }
      }
    }

    stage('branch') {

      when {
        not{
         triggeredBy cause: "GenericCause"
        }
        not{
         branch "master"
        }
      }

      stages {
        stage('Create VMs') {
          steps {
          parallel(
                "CEGA": {
                          sh '''
                            gradle :cluster:createCEGAMachine -Pmachine=CEGA-${ID} --stacktrace -i
                          '''
                },
                "LEGA Public": {
                          sh '''
                            gradle :cluster:createLEGAPublicMachine -Pmachine=LEGA-public-${ID} --stacktrace -i
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
                gradle :common:createConfiguration \
                    -PlegaPrivateIP=$(docker-machine ip LEGA-private-${ID}) \
                    --stacktrace

                gradle :cega:createConfiguration \
                    -Pmachine=CEGA-${ID} \
                    --stacktrace

                gradle :lega-private:createConfiguration \
                    -Pmachine=LEGA-private-${ID} \
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
                "CEGA": {
                          sh '''
                            gradle :cega:deployStack -Pmachine=CEGA-${ID} --stacktrace
                          '''
                },
                "LEGA Public": {
                          sh '''
                            gradle :lega-public:deployStack -Pmachine=LEGA-public-${ID} --stacktrace
                          '''
                },
                "LEGA Private": {
                          sh '''
                            gradle :lega-private:deployStack -Pmachine=LEGA-private-${ID} --stacktrace
                          '''
                }
              )
          }
        }

        stage('Initialization') {
          steps {
            sh '''
              sleep 120
            '''
          }
        }

        stage('Test') {
          steps {
            sh '''
              gradle verify -PcegaIP=$(docker-machine ip CEGA-${ID}) -PlegaPublicIP=$(docker-machine ip LEGA-public-${ID}) -PlegaPrivateIP=$(docker-machine ip LEGA-private-${ID}) --stacktrace
            '''
          }
        }
      }
      post('Remove VM') {
        failure{
          sh '''
            gradle :cluster:serviceLogs -Pmachine=LEGA-public-${ID} -Pservice=inbox --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-public-${ID} -Pservice=mq --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=mq --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=ingest --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=db --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=vault-s3 --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=LEGA-private-${ID} -Pservice=verify --stacktrace -i
          '''
        }
        cleanup {
          sh 'docker-machine rm -y CEGA-${ID} LEGA-public-${ID} LEGA-private-${ID}'
        }
      }
    }

    stage('master') {

      environment {
        ENV = 'staging'
        LEGA_private_IP = sh(
                        script: "printf \$(docker-machine ip lega-private-staging)",
                        returnStdout: true
                )
        LEGA_public_IP = sh(
                        script: "printf \$(docker-machine ip lega-public-staging)",
                        returnStdout: true
                )
        CEGA_MQ_CONNECTION=credentials('CEGA_MQ_CONNECTION')
        CEGA_USERS_CONNECTION=credentials('CEGA_USERS_CONNECTION')
        CEGA_USERS_CREDENTIALS=credentials('CEGA_USERS_CREDENTIALS')
      }

      when {
        not {
          triggeredBy cause: "GenericCause"
        }
        branch "master"
      }

      stages {
        stage('Tear down') {
          steps {
              sh '''
                gradle :cega:removeCEGATmpTask

                gradle :lega-private:removeStack -Pmachine=lega-private-staging --stacktrace
                gradle :lega-public:removeStack -Pmachine=lega-public-staging --stacktrace

                sleep 10

                gradle prune -Pmachine=lega-private-staging --stacktrace
                gradle prune -Pmachine=lega-public-staging --stacktrace
              '''
          }
        }

        stage('Bootstrap') {
          steps {
              sh '''
                gradle :common:createConfiguration \
                    -PlegaPrivateIP=${LEGA_private_IP} \
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
              sleep 180
            '''
          }
        }

        stage('Test') {
          steps {
            sh '''
              gradle verify -PlegaPublicIP=${LEGA_public_IP} -PlegaPrivateIP=${LEGA_private_IP} --stacktrace
            '''
          }
        }
      }

      post('logging') {
        failure {
          sh '''
            gradle :cluster:serviceLogs -Pmachine=lega-public-staging -Pservice=inbox --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=lega-public-staging -Pservice=mq --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=lega-private-staging -Pservice=mq --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=lega-private-staging -Pservice=ingest --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=lega-private-staging -Pservice=db --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=lega-private-staging -Pservice=vault-s3 --stacktrace -i
            gradle :cluster:serviceLogs -Pmachine=lega-private-staging -Pservice=verify --stacktrace -i
          '''
        }
        cleanup {
          cleanWs()
        }
      }

    }

  }

  post {
      always {
        chuckNorris()
      }

      success {
        slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
      }

      failure {
        slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
      }
    }
    
}
