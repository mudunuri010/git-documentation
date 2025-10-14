pipeline {
    agent any
    
    parameters {
        // Environment selection (DROPDOWN)
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Select deployment environment')
        
        // Image and container parameters
        string(name: 'IMAGE_NAME', defaultValue: 'saimudunuri9/git-documentation', description: 'Docker image name (local only)')
        string(name: 'IMAGE_TAG', defaultValue: '4', description: 'Docker image tag/version')
        
        // Git parameters
        string(name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch to checkout')
        string(name: 'GIT_URL', defaultValue: 'https://github.com/mudunuri010/git-documentation', description: 'Git repository URL')
        
        // Optional: Control deployment behavior
        booleanParam(name: 'FORCE_REMOVE', defaultValue: true, description: 'Force remove existing container?')
    }
    
    environment {
        IMAGE_NAME = "${params.IMAGE_NAME}"
        CONTAINER_NAME = "${params.CONTAINER_NAME}"
        PORT = "${params.PORT}"
    }
    
    stages {
        stage('Display Configuration') {
            steps {
                echo "=== Deployment Configuration ==="
                echo "Image: ${params.IMAGE_NAME}:${params.IMAGE_TAG}"
                echo "Container: ${params.CONTAINER_NAME}"
                echo "Port: ${params.PORT}:3000"
                echo "Branch: ${params.GIT_BRANCH}"
                echo "Repository: ${params.GIT_URL}"
                echo "=============================="
            }
        }
        
        stage('Checkout') {
            steps {
                git branch: "${params.GIT_BRANCH}", url: "${params.GIT_URL}"
            }
        }
        
        stage('Deploy Locally') {
            steps {
                script {
                    if (params.FORCE_REMOVE) {
                        sh "docker rm -f ${params.CONTAINER_NAME} || true"
                    }
                }
                sh "docker run -d -p ${params.PORT}:3000 --name ${params.CONTAINER_NAME} ${params.IMAGE_NAME}:${params.IMAGE_TAG}"
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo "Checking running containers..."
                sh 'docker ps'
                echo "Container ${params.CONTAINER_NAME} is running on port ${params.PORT}"
            }
        }
    }
    
    post {
        success {
            echo "✅ Deployment successful! Access your app at http://localhost:${params.PORT}"
        }
        failure {
            echo "❌ Deployment failed. Check the logs above."
        }
    }
}

