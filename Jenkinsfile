properties([
    parameters([
        // Static dropdown: environment
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Select deployment environment'),

        // Active Choice Parameter: server depends on environment
        [$class: 'CascadeChoiceParameter',
         choiceType: 'PT_SINGLE_SELECT',
         description: 'Select the server dynamically',
         name: 'SERVER',
         referencedParameters: 'ENVIRONMENT',
         script: [
             $class: 'GroovyScript',
             fallbackScript: [classpath: [], sandbox: true, script: 'return ["No servers available"]'],
             script: [classpath: [], sandbox: true, script: '''
                 if (ENVIRONMENT == "dev") {
                     return ["dev-server-1","dev-server-2"]
                 } else if (ENVIRONMENT == "staging") {
                     return ["staging-server-1","staging-server-2"]
                 } else {
                     return ["prod-server-1","prod-server-2"]
                 }
             ''']
         ]
        ],

        // Active Choice Reactive Reference: automatically suggest container name based on server
        [$class: 'CascadeChoiceParameter',
         choiceType: 'PT_SINGLE_SELECT',
         description: 'Auto-generate container name based on server',
         name: 'CONTAINER_NAME',
         referencedParameters: 'SERVER',
         script: [
             $class: 'GroovyScript',
             fallbackScript: [classpath: [], sandbox: true, script: 'return ["default-container"]'],
             script: [classpath: [], sandbox: true, script: '''
                 return [SERVER + "-container"]
             ''']
         ]
        ],

        // Docker image and port
        string(name: 'IMAGE_NAME', defaultValue: 'saimudunuri9/git-documentation', description: 'Docker image name'),
        string(name: 'IMAGE_TAG', defaultValue: '4', description: 'Docker image tag/version'),
        string(name: 'PORT', defaultValue: '8080', description: 'Host port to expose'),

        // Git parameters
        string(name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch to checkout'),
        string(name: 'GIT_URL', defaultValue: 'https://github.com/mudunuri010/git-documentation', description: 'Git repository URL'),

        booleanParam(name: 'FORCE_REMOVE', defaultValue: true, description: 'Force remove existing container?')
    ])
])

pipeline {
    agent any

    environment {
        IMAGE_NAME = "${params.IMAGE_NAME}"
        IMAGE_TAG = "${params.IMAGE_TAG}"
        CONTAINER_NAME = "${params.CONTAINER_NAME}"
        PORT = "${params.PORT}"
        TARGET_SERVER = "${params.SERVER}"
        ENV = "${params.ENVIRONMENT}"
    }

    stages {
        stage('Display Configuration') {
            steps {
                echo "=== Deployment Configuration ==="
                echo "Environment: ${ENV}"
                echo "Target Server: ${TARGET_SERVER}"
                echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
                echo "Container: ${CONTAINER_NAME}"
                echo "Port: ${PORT}"
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

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}..."
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Deploy') {
            steps {
                script {
                    if (params.FORCE_REMOVE) {
                        echo "Removing existing container if it exists..."
                        sh "docker rm -f ${CONTAINER_NAME} || true"
                    }

                    echo "Deploying container to ${TARGET_SERVER}..."
                    
                    def hostPort = PORT
                    if (ENV == 'dev') hostPort = '8081'
                    if (ENV == 'staging') hostPort = '8082'
                    if (ENV == 'prod') hostPort = '80'

                    sh "docker run -d -p ${hostPort}:3000 --name ${CONTAINER_NAME} ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo "Verifying running containers on ${TARGET_SERVER}..."
                sh 'docker ps'
                echo "Container ${CONTAINER_NAME} should be running on port ${PORT}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful! Access your app at http://localhost:${PORT}"
        }
        failure {
            echo "❌ Deployment failed. Check the logs above."
        }
    }
}


