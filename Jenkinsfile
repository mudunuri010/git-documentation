pipeline {
    agent any
    environment {
        IMAGE_NAME = "saimudunuri9/git-documentation"
        CONTAINER_NAME = "git-doc"
        PORT = "3000"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/mudunuri010/git-documentation'
            }
        }
        stage('Deploy Locally') {
            steps {
                sh 'docker rm -f ${CONTAINER_NAME} || true'
                sh 'docker run -d -p ${PORT}:3000 --name ${CONTAINER_NAME} ${IMAGE_NAME}:4'
            }
        }
        stage('Verify Deployment') {
            steps {
                sh 'docker ps'
            }
        }
    }
}

