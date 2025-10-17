properties([
    parameters([
        // Static dropdown for environment
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Select deployment environment'),

        // Active Choice: SERVER calls get_servers.bat
        [$class: 'CascadeChoiceParameter',
         choiceType: 'PT_SINGLE_SELECT',
         description: 'Select the server dynamically from script',
         name: 'SERVER',
         referencedParameters: 'ENVIRONMENT',
         script: [
             $class: 'GroovyScript',
             fallbackScript: [classpath: [], sandbox: true, script: 'return ["No servers available"]'],
             script: [classpath: [], sandbox: true, script: '''
                 def env = ENVIRONMENT ?: "dev"
                 // Execute the Windows Batch script
                 def command = "cmd /c C:\\apps\\scripts\\get_servers.bat ${env}"
                 def process = command.execute()
                 process.waitFor()
                 
                 def output = process.in.text.trim()
                 
                 if (process.exitValue() == 0 && output) {
                     // Split by Windows newlines (\r\n) or just \n for robustness
                     return output.split(/\\r?\\n/) as List
                 } else {
                     return ["Error fetching servers"]
                 }
             ''']
         ]
        ],

        // Active Choice: CONTAINER_NAME calls generate_container_name.bat
        [$class: 'CascadeChoiceParameter',
         choiceType: 'PT_SINGLE_SELECT',
         description: 'Auto-generate container name from script',
         name: 'CONTAINER_NAME',
         referencedParameters: 'SERVER',
         script: [
             $class: 'GroovyScript',
             fallbackScript: [classpath: [], sandbox: true, script: 'return ["default-container"]'],
             script: [classpath: [], sandbox: true, script: '''
                 if (!SERVER) { return ["default-container"] }
                 
                 // Execute the Windows Batch script
                 def command = "cmd /c C:\\apps\\scripts\\generate_container_name.bat ${SERVER}"
                 def process = command.execute()
                 process.waitFor()

                 def output = process.in.text.trim()

                 if (process.exitValue() == 0 && output) {
                     return [output]
                 } else {
                     return ["error-container"]
                 }
             ''']
         ]
        ],
        
        // Static parameters remain the same
        string(name: 'IMAGE_NAME', defaultValue: 'saimudunuri9/git-documentation', description: 'Docker image name'),
        string(name: 'IMAGE_TAG', defaultValue: '4', description: 'Docker image tag/version'),
        string(name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch to checkout'),
        string(name: 'GIT_URL', defaultValue: 'https://github.com/mudunuri010/git-documentation', description: 'Git repository URL'),
        booleanParam(name: 'FORCE_REMOVE', defaultValue: true, description: 'Force remove existing container before deploy?')
    ])
])

pipeline {
    // Ensure this agent is a Windows node
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
                echo "Target Server:   ${TARGET_SERVER}"
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
                // Use the 'bat' step for Windows commands
                bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    // Call get_port.bat to get the port
                    def hostPort = bat(script: "C:\\apps\\scripts\\get_port.bat ${ENV}", returnStdout: true).trim()
                    echo "Using port '${hostPort}' for environment '${ENV}'"

                    if (params.FORCE_REMOVE) {
                        echo "Attempting to remove existing container..."
                        // Use '|| exit 0' for non-fatal errors in Windows
                        bat "docker rm -f ${CONTAINER_NAME} || exit 0"
                    }

                    echo "Deploying container ${CONTAINER_NAME} on ${TARGET_SERVER}..."
                    bat "docker run -d -p ${hostPort}:3000 --name ${CONTAINER_NAME} ${IMAGE_NAME}:${IMAGE_TAG}"

                    env.DEPLOYED_PORT = hostPort
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo "Verifying active containers..."
                bat "docker ps"
                echo "Container ${CONTAINER_NAME} should be running and accessible on port ${env.DEPLOYED_PORT}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful!"
            echo "🌍 Access your application at: http://<your-server-ip>:${env.DEPLOYED_PORT}"
        }
        failure {
            echo "❌ Deployment failed. Please review the console output for errors."
        }
    }
}
