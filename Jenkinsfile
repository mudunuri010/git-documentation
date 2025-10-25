Jenkinsfile - Full Automation
// Define Docker image name prefix (e.g., your Docker Hub username)
def dockerImagePrefix = 'saimudunuri9' // Change if needed

properties([
    parameters([
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'qa', 'staging', 'prod'],
            description: 'Select deployment environment'
        ), [cite: 7]
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT', // [cite: 8]
            description: 'Select the server dynamically from script',
            name: 'SERVER',
            referencedParameters: 'ENVIRONMENT',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [ classpath: [], script: 'return ["FALLBACK-CHECK-LOGS"]' ], // [cite: 9]
                script: [ // [cite: 10]
                    classpath: [], // [cite: 9]
                    script: '''
                        try {
                            def env = ENVIRONMENT ?: "dev"
                            def command = ["sh", "/var/jenkins_home/scripts/get_servers.sh", env] // Execute script [cite: 11]
                            def process = command.execute() // [cite: 11]
                            process.waitFor() // [cite: 11]
                            def output = process.in.text.trim() // [cite: 11]
                            if (process.exitValue() != 0 || !output) { return ["ERR-ScriptFailed"] } // [cite: 12, 13, 14]
                            def servers = output.split(/\\n/) as List // [cite: 15]
                            return servers ?: ["ERR-NoServers"] // [cite: 15, 16]
                        } catch (Exception e) { return ["EXCEPTION-" + e.class.simpleName] } // [cite: 17]
                    '''
                ]
            ]
        ],
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT', // [cite: 8]
            description: 'Auto-generate container name from script',
            name: 'CONTAINER_NAME', // [cite: 18]
            referencedParameters: 'SERVER',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [ classpath: [], script: 'return ["default-container"]' ], // [cite: 19]
                script: [ // [cite: 20]
                    classpath: [], // [cite: 19]
                    script: '''
                        try { // [cite: 20]
                            if (!SERVER || SERVER.startsWith("ERR-") || SERVER.startsWith("EXCEPTION-")) { return ["error-server-param"] } // [cite: 21]
                            def command = ["sh", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER] // Execute script
                            def process = command.execute() // [cite: 22]
                            process.waitFor() // [cite: 22]
                            def output = process.in.text.trim() // [cite: 22]
                            if (process.exitValue() == 0 && output) { return [output] } // [cite: 23]
                            else { return ["error-generating-name"] } // [cite: 24]
                        } catch (Exception e) { return ["exception-generating-name"] } // [cite: 25]
                    '''
                ]
            ]
        ],
        // IMAGE_NAME and IMAGE_TAG are now generated dynamically
        string(
            name: 'GIT_BRANCH',
            defaultValue: 'master',
            description: 'Git branch to checkout'
        ), // [cite: 27]
        string(
            name: 'GIT_URL',
            defaultValue: 'https://github.com/mudunuri010/git-documentation',
            description: 'Git repository URL'
        ), // [cite: 28]
        booleanParam(
            name: 'FORCE_REMOVE',
            defaultValue: true,
            description: 'Force remove existing container before deploy?'
        ) // [cite: 28]
    ])
])

pipeline {
    agent any // Runs on the Jenkins controller

    environment {
        // Define dynamic image name and tag using env and build number
        IMAGE_NAME_TAG = "${dockerImagePrefix}/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
        HOST_PORT = '' // Initialize HOST_PORT
    }

    triggers {
        // Poll the Git repository every 5 minutes
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Initialize & Get Port') { // [cite: 29]
            steps { // [cite: 29]
                script { // [cite: 29]
                    echo "Starting Build #${BUILD_NUMBER}"
                    echo "Image to Build/Deploy: ${env.IMAGE_NAME_TAG}"
                    echo "Environment:     ${params.ENVIRONMENT}" // [cite: 29]
                    echo "Target Server:   ${params.SERVER}" // [cite: 30] // Note: Server param is less relevant for local docker deploy
                    echo "Container Name:  ${params.CONTAINER_NAME}" // [cite: 30]
                    echo "Git Branch:      ${params.GIT_BRANCH}" // [cite: 31]
                    echo "Git URL:         ${params.GIT_URL}" // [cite: 28]
                    echo "Force Remove:    ${params.FORCE_REMOVE}" // [cite: 31]

                    // Get port for environment
                    def portCommand = ["sh", "/var/jenkins_home/scripts/get_port.sh", params.ENVIRONMENT] // Execute script
                    def portProcess = portCommand.execute() // [cite: 32]
                    portProcess.waitFor() // [cite: 32]
                    env.HOST_PORT = portProcess.in.text.trim() // [cite: 32]
                    if (portProcess.exitValue() != 0 || !env.HOST_PORT) {
                        error "Failed to get port for environment ${params.ENVIRONMENT}"
                    }
                    echo "Using port '${env.HOST_PORT}' for environment '${params.ENVIRONMENT}'" // [cite: 32]
                    echo "------------------------------------------" // [cite: 32]
                } // [cite: 33]
            }
        }

        stage('Checkout Code') {
            steps {
                echo "=== Checking out code from ${params.GIT_URL} branch ${params.GIT_BRANCH} ==="
                // Use git-credentials ID defined in jenkins.casc.yml
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${params.GIT_BRANCH}"]],
                    userRemoteConfigs: [[url: params.GIT_URL, credentialsId: 'git-credentials']],
                    extensions: [[$class: 'CleanBeforeCheckout']]
                ])
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "=== Building Docker Image: ${env.IMAGE_NAME_TAG} ==="
                    // Assumes Dockerfile is in the root of the checked-out repo [cite: 5]
                    def customImage = docker.build(env.IMAGE_NAME_TAG, ".")
                    echo "✅ Docker Image Built Successfully"

                    // Optional: Push image (requires Docker Hub credentials)
                    /*
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials-id') {
                        customImage.push()
                    }
                    */
                }
            }
        }

        stage('Cleanup Existing Container') { // [cite: 34]
            when {
                expression { params.FORCE_REMOVE == true } // [cite: 34]
            }
            steps { // [cite: 34]
                script { // [cite: 34]
                    echo "=== Checking for existing container: ${params.CONTAINER_NAME} ===" // [cite: 34]
                    // Ignore errors if container doesn't exist
                    sh(script: "docker stop ${params.CONTAINER_NAME} || true", returnStatus: true) // [cite: 36]
                    sh(script: "docker rm ${params.CONTAINER_NAME} || true", returnStatus: true) // [cite: 37]
                    echo "Cleanup finished (errors ignored)." // [cite: 38]
                }
            }
        }

        stage('Deploy Container') { // [cite: 39]
            steps { // [cite: 39]
                script { // [cite: 39]
                    echo "=== Deploying Application Container: ${params.CONTAINER_NAME} ===" // [cite: 39]
                    // Run the image built in the previous stage
                    sh """
                        docker run -d \\
                            --name ${params.CONTAINER_NAME} \\
                            -p ${env.HOST_PORT}:3000 \\
                            ${env.IMAGE_NAME_TAG}
                    """ // [cite: 40]
                    echo "✅ Deployment successful!" // [cite: 41]
                }
            }
        }

        stage('Verify Deployment') { // [cite: 42]
            steps { // [cite: 42]
                script { // [cite: 42]
                    echo "=== Verifying Deployment ===" // [cite: 42]
                    sleep 5 // Give container a moment to start
                    sh "docker ps | grep ${params.CONTAINER_NAME}" // [cite: 43]
                    echo "✅ Container is running!" // [cite: 43]
                    // Display the URL here too
                    echo "Application should be live at: http://localhost:${env.HOST_PORT}"
                }
            }
        }
    }

    post {
        success { // [cite: 44]
            script { // [cite: 44]
                echo "🎉 Pipeline successful!" // [cite: 44]
                echo "Application '${params.CONTAINER_NAME}' for environment '${params.ENVIRONMENT}' is live."
                // Use the env var captured earlier
                echo "Access it at: http://localhost:${env.HOST_PORT}"
            }
        }
        failure { // [cite: 45]
            echo "❌ Pipeline failed. Please review the console output for errors." // [cite: 45]
        }
    }
}