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

        // Active Choice Reactive Reference: auto-generate container name based on server
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

        // Docker image and port (default)
        string(name: 'IMAGE_NAME', defaultValue: 'saimudunuri9/git-documentation', description: 'Docker image name'),
        string(name: 'IMAGE_TAG', defaultValue: '4', description: 'Docker image tag/version'),
        string(name: 'PORT', defaultValue: '8080', description: 'Default host port (used if no override)'),

        // Git parameters
        string(name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch to checkout'),
        string(name: 'GIT_URL', defaultValue: 'https://github.com/mudunuri010/git-documentation', description: 'Git repository URL'),

        booleanParam(name: 'FORCE_REMOVE', defaultValue: true, description: 'Force remove existing container before deploy?')
    ])
])

pipeline {
    agent any

    environment {
        IMAGE_NAME = "${params.IMAGE_NAME}"
        IMAGE_TAG = "${params.IMAGE_TAG}"
        CONTAINER_NAME = "${params.CONTAINER_NAME}"
        TARGET_SERVER = "${params.SERVER}"
        ENV = "${params.ENVIRONMENT}"
        GIT_BRANCH = "${params.GIT_BRANCH}"
        GIT_URL = "${params.GIT_URL}"
    }

    stages {
        stage('Display Configuration') {
            steps {
                echo "=============================="
                echo "Environment: ${ENV}"
                echo "Target Server: ${TARGET_SERVER}"
                echo "Container: ${CONTAINER_NAME}"
                echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
                echo "Git Branch: ${GIT_BRANCH}"
                echo "Git Repo: ${GIT_URL}"
                echo "=============================="
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_URL}"
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
                    // ✅ Dynamically decide host port based on environment
                    def hostPort = ""
                    if (ENV == 'dev') hostPort = '8085'
                    else if (ENV == 'staging') hostPort = '8086'
                    else if (ENV == 'prod') hostPort = '80'
                    else hostPort = params.PORT  // fallback

                    echo "Using port: ${hostPort} for environment: ${ENV}"

                    if (params.FORCE_REMOVE) {
                        echo "Removing existing container (if any)..."
                        sh "docker rm -f ${CONTAINER_NAME} || true"
                    }

                    echo "Deploying container ${CONTAINER_NAME} on ${TARGET_SERVER}..."
                    sh "docker run -d -p ${hostPort}:3000 --name ${CONTAINER_NAME} ${IMAGE_NAME}:${IMAGE_TAG}"

                    // Save dynamic port for later use
                    env.DEPLOYED_PORT = hostPort
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo "Verifying containers..."
                sh "docker ps"
                echo "Container ${CONTAINER_NAME} should be accessible on port ${env.DEPLOYED_PORT}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful!"
            echo "🌍 Access your app at: http://localhost:${env.DEPLOYED_PORT}"
        }
        failure {
            echo "❌ Deployment failed. Check the above logs."
        }
    }
}
