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
                script {
                    // Stop and remove existing container if it exists
                    sh """
                    CONTAINER_ID=\$(docker ps -a -q -f name=${CONTAINER_NAME})
                    if [ ! -z "\$CONTAINER_ID" ]; then
                        docker stop \$CONTAINER_ID
                        docker rm \$CONTAINER_ID
                    fi
                    """

                    // Run the container locally from the pulled image
                    sh "docker run -d -p ${PORT}:3000 --name ${CONTAINER_NAME} ${IMAGE_NAME}:4"
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                sh "docker ps"
            }
        }
    }
}


