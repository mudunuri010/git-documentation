properties([
    parameters([
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Select deployment environment'),

        [$class: 'CascadeChoiceParameter',
         choiceType: 'PT_SINGLE_SELECT',
         description: 'Select the server dynamically from script',
         name: 'SERVER',
         referencedParameters: 'ENVIRONMENT',
         script: [
             $class: 'GroovyScript',
             fallbackScript: [classpath: [], sandbox: false, script: 'return ["FALLBACK-CHECK-LOGS"]'],
             script: [classpath: [], sandbox: false, script: '''
                 try {
                     def env = ENVIRONMENT ?: "dev"
                     def command = ["/var/jenkins_home/scripts/get_servers.sh", env]
                     def process = command.execute()
                     process.waitFor()
                     
                     def output = process.in.text.trim()
                     def errors = process.err.text.trim()
                     def exitCode = process.exitValue()
                     
                     if (exitCode != 0) {
                         return ["ERR-ExitCode-" + exitCode]
                     }
                     
                     if (!output || output.isEmpty()) {
                         return ["ERR-NoOutput"]
                     }
                     
                     def servers = output.split(/\n/) as List
                     
                     if (!servers || servers.isEmpty()) {
                         return ["ERR-EmptyList"]
                     }
                     
                     return servers
                     
                 } catch (Exception e) {
                     return ["EXCEPTION-" + e.class.simpleName]
                 }
             ''']
         ]
        ],

        [$class: 'CascadeChoiceParameter',
         choiceType: 'PT_SINGLE_SELECT',
         description: 'Auto-generate container name from script',
         name: 'CONTAINER_NAME',
         referencedParameters: 'SERVER',
         script: [
             $class: 'GroovyScript',
             fallbackScript: [classpath: [], sandbox: false, script: 'return ["default-container"]'],
             script: [classpath: [], sandbox: false, script: '''
                 try {
                     if (!SERVER || SERVER.startsWith("ERR-") || SERVER.startsWith("EXCEPTION-")) {
                         return ["error-container"]
                     }
                     
                     def command = ["/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
                     def process = command.execute()
                     process.waitFor()

                     def output = process.in.text.trim()

                     if (process.exitValue() == 0 && output) {
                         return [output]
                     } else {
                         return ["error-container"]
                     }
                 } catch (Exception e) {
                     return ["exception-container"]
                 }
             ''']
         ]
        ],
        
        string(name: 'IMAGE_NAME', defaultValue: 'saimudunuri9/git-documentation', description: 'Docker image name'),
        string(name: 'IMAGE_TAG', defaultValue: '4', description: 'Docker image tag/version'),
        string(name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch to checkout'),
        string(name: 'GIT_URL', defaultValue: 'https://github.com/mudunuri010/git-documentation', description: 'Git repository URL'),
        booleanParam(name: 'FORCE_REMOVE', defaultValue: true, description: 'Force remove existing container before deploy?')
    ])
])

pipeline {
    agent any

    environment {
        IMAGE_NAME     = "${params.IMAGE_NAME}"
        IMAGE_TAG      = "${params.IMAGE_TAG}"
        CONTAINER_NAME = "${params.CONTAINER_NAME}"
        TARGET_SERVER  = "${params.SERVER}"
        ENV            = "${params.ENVIRONMENT}"
        GIT_BRANCH     = "${params.GIT_BRANCH}"
        GIT_URL        = "${params.GIT_URL}"
    }

    stages {
        stage('Display Configuration') {
            steps {
                echo "Deploying with the following configuration:"
                echo "------------------------------------------"
                echo "Environment:     ${ENV}"
                echo "Target Server:   ${params.SERVER}"
                echo "Container Name:  ${CONTAINER_NAME}"
                echo "Image:           ${IMAGE_NAME}:${IMAGE_TAG}"
                echo "Git Branch:      ${GIT_BRANCH}"
                echo "------------------------------------------"
            }
        }

        stage('Checkout Code') {
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

        stage('Deploy Container') {
            steps {
                script {
                    def hostPort = sh(script: "/var/jenkins_home/scripts/get_port.sh ${ENV}", returnStdout: true).trim()
                    echo "Using port '${hostPort}' for environment '${ENV}'"

                    if (params.FORCE_REMOVE) {
                        echo "Attempting to remove existing container..."
                        sh "docker rm -f ${CONTAINER_NAME} || true"
                    }

                    echo "Deploying container ${CONTAINER_NAME} on ${TARGET_SERVER}..."
                    sh "docker run -d -p ${hostPort}:3000 --name ${CONTAINER_NAME} ${IMAGE_NAME}:${IMAGE_TAG}"

                    env.DEPLOYED_PORT = hostPort
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo "Verifying active containers..."
                sh "docker ps"
                echo "Container ${CONTAINER_NAME} should be running and accessible on port ${env.DEPLOYED_PORT}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful!"
            echo "🌍 Access your application at: http://localhost:${env.DEPLOYED_PORT}"
        }
        failure {
            echo "❌ Deployment failed. Please review the console output for errors."
        }
    }
}
