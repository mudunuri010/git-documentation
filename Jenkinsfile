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
                    bat """
                    docker ps -a -q -f name=%CONTAINER_NAME% > temp.txt
                    set /p CONTAINER_ID=<temp.txt
                    if not "%CONTAINER_ID%"=="" (
                        docker stop %CONTAINER_ID%
                        docker rm %CONTAINER_ID%
                    )
                    del temp.txt
                    """

                    // Run the container locally from the pulled image
                    bat "docker run -d -p %PORT%:3000 --name %CONTAINER_NAME% %IMAGE_NAME%:4"
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                bat "docker ps"
            }
        }
    }
}

